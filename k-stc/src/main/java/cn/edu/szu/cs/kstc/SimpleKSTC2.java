package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.entity.Coordinate;
import cn.edu.szu.cs.entity.KSTCQuery;
import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.service.IRelatedObjectService;
import cn.edu.szu.cs.util.CommonUtil;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *  Simple implementation of KSTC algorithm.
 * @author Whitence
 * @date 2023/10/1 11:08
 * @version 1.0
 */

public class SimpleKSTC2<T extends RelatedObject> implements KSTC<T> {
    // Some tools
    /**
     * logger
     */
    private static final Log logger = LogFactory.get();
    /**
     * Timer for further performance analysis
     */
    private static final InheritableThreadLocal<TimeInterval> inheritableThreadLocal = new InheritableThreadLocal<>();

    /**
     * Maximum timeout per request.
     */
    private static final long DEFAULT_TIMEOUT = 10_000L;
    /**
     * Thread pool for asynchronous computing
     */
    private static class ThreadPoolExecutorHolder{

        public static ThreadPoolExecutor threadPool = null;
        static {
            int processors = Runtime.getRuntime().availableProcessors();
            threadPool=new ThreadPoolExecutor(
                    processors+1,
                    2*processors,
                    30,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(10),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
            logger.debug("Thread pool initialized successfully.");
        }
    }

    /**
     * rTree is used to store the spatial information of related objects.
     */
    private RTree<String,GeoPointDouble> rTree;
    /**
     * Preserve the extensibility of the way to obtain related objects.
     */
    private IRelatedObjectService relatedObjectService;

    /**
     * The main cache is used to cache results. Using soft references ensures that there will be no out-of-memory errors.
     */
    private static volatile SoftReference<LFUCache<String,Object>> mainCache = null;
    private LFUCache<String,Object> mainCache(){
        if(mainCache == null || mainCache.get() == null){
            synchronized (SimpleKSTC2.class){
                if(mainCache == null){
                    mainCache = new SoftReference<>(CacheUtil.newLFUCache(128));
                    logger.debug("Main cache initialized successfully.");
                }
            }
        }
        return mainCache.get();
    }

    /**
     * Trie is used to construct inverted index for prefix matching.
     */
    private Trie root;
    private static class Trie{
        boolean isWord;
        Map<Character, Trie> children;
        List<RelatedObject> objs;

        void put(String keyword,RelatedObject object){
            Trie cur=this;
            for (char ch : keyword.toLowerCase().toCharArray()) {
                if(cur.children == null){
                    cur.children = new HashMap<>();
                }
                cur.children.putIfAbsent(ch,new Trie());
                cur=cur.children.get(ch);
            }
            cur.isWord=true;
            if(cur.objs == null){
                cur.objs=new LinkedList<>();
            }
            cur.objs.add(object);
        }

        List<RelatedObject> search(String keyword){
            Trie cur = this;
            for (char ch : keyword.toCharArray()) {
                if(cur.children == null){
                    return Collections.emptyList();
                }
                cur=cur.children.get(ch);
            }
            List<RelatedObject> result = new LinkedList<>();
            Queue<Trie> queue = new LinkedList<>();
            queue.offer(cur);
            while (!queue.isEmpty()){
                Trie node = queue.poll();
                if(node.isWord){
                    result.addAll(node.objs);
                }
                if(node.children != null){
                    queue.addAll(node.children.values());
                }
            }
            return result;
        }
    }


    private static volatile SoftReference<LRUCache<String,List<RelatedObject>>> invertedIndexCache = null;
    private LRUCache<String,List<RelatedObject>> invertedIndexCache(){
        if(invertedIndexCache == null){
            synchronized (SimpleKSTC2.class){
                if(invertedIndexCache == null){
                    invertedIndexCache=new SoftReference<>(CacheUtil.newLRUCache(32,DEFAULT_TIMEOUT));
                    logger.debug("Inverted index cache initialized successfully.");
                }
            }
        }
        return invertedIndexCache.get();
    }

    public SimpleKSTC2(IRelatedObjectService relatedObjectService) {
        Assert.notNull(relatedObjectService,"relatedObjectService is null.");
        this.relatedObjectService=relatedObjectService;
        List<RelatedObject> objects = relatedObjectService.getAll();
        Assert.isTrue(objects instanceof RandomAccess,"If RandomAccess is not supported, the construction of rtree will become very slow.");
        //buildrTree(objects);
        buildTrieInvertedIndex(objects);
    }
    private void buildrTree(List<RelatedObject> objects){
        List<Entry<String, GeoPointDouble>> entryList = objects.stream()
                .map(object -> Entries.entry(object.getObjectId(), GeoPointDouble.create(object.getCoordinate().getLongitude(), object.getCoordinate().getLatitude())))
                .collect(Collectors.toList());
        rTree = RTree.star().create(entryList);
        logger.debug("Rtree initialized successfully.");
    }
    private void buildTrieInvertedIndex(List<RelatedObject> objects){
        root=new Trie();
        for (RelatedObject relatedObject : objects) {
            for (String label : relatedObject.getLabels()) {
                root.put(label,relatedObject);
            }
        }
        logger.debug("Trie inverted index initialized successfully.");
    }



    private Set<RelatedObject> getRelatedObjectIds(String keyword){
        if(StrUtil.isBlank(keyword)){
            return Collections.emptySet();
        }
        List<RelatedObject> result = invertedIndexCache().get(keyword);
        if(result == null){
            result = root.search(keyword);
            invertedIndexCache().put(keyword,result);
        }
        return new HashSet<>(result);
    }

    public Set<RelatedObject> getRelatedObjectIds(List<String> keyword){
        if(keyword == null || keyword.isEmpty()){
            return Collections.emptySet();
        }
        Set<RelatedObject> result = getRelatedObjectIds(keyword.get(0));
        for (int i = 1; i < keyword.size(); i++) {
            result.retainAll(getRelatedObjectIds(keyword.get(i)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private PriorityQueue<T> getSList(List<String> keywords,double[] coordinate,double maxDist,Comparator<T> comparator){
        Set<RelatedObject> objs = getRelatedObjectIds(keywords);
        return objs
                .stream()
                .filter(object -> CommonUtil.calculateDistance(coordinate,object.getCoordinate()) < maxDist)
                .map(object -> (T)object)
                .collect(Collectors.toCollection(()->new PriorityQueue<>(comparator)));

    }
    @SuppressWarnings("unchecked")
    private List<T> rangeQuery(List<String> keywords,double[] coordinate,double epsilon){
        Set<RelatedObject> relatedObjectIds = getRelatedObjectIds(keywords);
        return relatedObjectIds
                .stream()
                .filter(object -> CommonUtil.calculateDistance(
                        object.getCoordinate(),
                        coordinate
                ) < epsilon)
                .map(obj->(T)obj)
                .collect(Collectors.toCollection(LinkedList::new));



    }

    /**
     * cut off precision of cache key.
     * @param KSTCQuery
     * @return
     */
    private String getCacheKey(KSTCQuery KSTCQuery){
        Coordinate coordinate = Coordinate.create(
                Math.round(KSTCQuery.getCoordinate()[0] * 1000.0) / 1000.0,
                Math.round(KSTCQuery.getCoordinate()[1] * 1000.0) / 1000.0
        );
        long maxDist = Math.round(KSTCQuery.getMaxDistance());
        long epsilon = Math.round(KSTCQuery.getEpsilon());
        return coordinate+"_"+ KSTCQuery.getKeywords()+"_"+epsilon+"_"+ KSTCQuery.getMinPts()+"_"+maxDist;
    }



    private List<Set<T>> basic(KSTCQuery KSTCQuery){
        logger.info("=> k-stc search start.");
        logger.info("=> k-stc query params: {}.", KSTCQuery.toString());
        getTimer().start("basic");
        List<Set<T>> list = doBasic(KSTCQuery);
        long intervalMs = getTimer().intervalMs("basic");
        releaseTimer();
        logger.info("=> k-stc search time cost:{} ms",intervalMs);
        return list;

    }


    /**
     * Basic algorithm of k-stc
     * @param KSTCQuery
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Set<T>> doBasic(KSTCQuery KSTCQuery){
        KSTCQuery.setKeywords(
                KSTCQuery.getKeywords().stream().map(String::toLowerCase).collect(Collectors.toList())
        );

        String key = getCacheKey(KSTCQuery);
        if(mainCache().containsKey(key)){
            Pair<List<Set<T>>, Integer> listIntegerPair = (Pair<List<Set<T>>, Integer>) mainCache().get(key);
            if(listIntegerPair.getValue()>= KSTCQuery.getK()){
                logger.debug("==> hit cache.");
                return listIntegerPair.getKey().stream().limit(KSTCQuery.getK()).collect(Collectors.toList());
            }
        }

        // sList
        Set<T> noises = new HashSet<>();
        // sort objs ascent by distance
        getTimer().start("timeout");
        PriorityQueue<T> sList = getSList(
                KSTCQuery.getKeywords(),
                KSTCQuery.getCoordinate(),
                KSTCQuery.getMaxDistance(),
                Comparator.comparingDouble(a -> CommonUtil.calculateDistance(KSTCQuery.getCoordinate(), a.getCoordinate()))
        );
        logger.info("==> getsList time cost:{}",getTimer().intervalMs("timeout"));
        List<Set<T>> rList = new ArrayList<>(KSTCQuery.getK());
        long intervalMs = getTimer().intervalMs("timeout");
        while(!sList.isEmpty() && rList.size()< KSTCQuery.getK() && intervalMs <= DEFAULT_TIMEOUT){
            // get the nearest obj
            T obj = sList.poll();
            Set<T> cluster = getCluster(obj, KSTCQuery, sList,noises);
            if(!cluster.isEmpty()){
                rList.add(cluster);
                logger.info("==> cluster {}, time cost:{}",rList.size(),getTimer().intervalMs("timeout"));
            }
            intervalMs = getTimer().intervalMs("timeout");
        }
        mainCache().put(
                key,
                new Pair<>(rList, KSTCQuery.getK())
        );
        // timeout, async
        if(!sList.isEmpty() && rList.size() < KSTCQuery.getK()){
            logger.info("Timeout. Async compute.");
            CompletableFuture.runAsync(
                    ()->continueComputeAsync(
                            rList,
                            sList,
                            KSTCQuery,
                            noises,
                            key
                    ),
                    ThreadPoolExecutorHolder.threadPool
            );
        }
        return rList;
    }

    private void continueComputeAsync(List<Set<T>> rList, PriorityQueue<T> sList, KSTCQuery KSTCQuery, Set<T> noises, String key){
        while(!sList.isEmpty() && rList.size()< KSTCQuery.getK()){
            // get the nearest obj
            T obj = sList.poll();
            Set<T> cluster = getCluster(obj, KSTCQuery, sList,noises);
            if(!cluster.isEmpty()){
                rList.add(cluster);
            }
        }
        mainCache().put(key,new Pair<>(rList, KSTCQuery.getK()));
        logger.info("Async compute finished.");
    }

    private Set<T> getCluster(T p,
                                           KSTCQuery q,
                                           PriorityQueue<T> sList,
                                           Set<T> noises){
        List<T> neighbors = rangeQuery(q.getKeywords(), p.getCoordinate(),q.getEpsilon());
        if(neighbors.size() < q.getMinPts()){
            // mark p
            noises.add(p);
            return Collections.emptySet();
        }
        Set<T> result = new HashSet<>(neighbors);
        neighbors.remove(p);
        while(!neighbors.isEmpty()){
            T neighbor = neighbors.remove(0);
            sList.remove(neighbor);
            List<T> neighborsTmp = rangeQuery(q.getKeywords(), neighbor.getCoordinate(),q.getEpsilon());
            if(neighborsTmp.size() >= q.getMinPts()){
                for (T obj : neighborsTmp) {
                    if(noises.contains(obj)){
                        result.add(obj);
                    }else if(!result.contains(obj)){
                        result.add(obj);
                        neighbors.add(obj);
                    }
                }
            }
        }
        return result;
    }

    /**
     * every thread has its own timer.
     * @return
     */
    private TimeInterval getTimer(){
        if(inheritableThreadLocal.get() == null){
            inheritableThreadLocal.set(new TimeInterval());
        }
        return inheritableThreadLocal.get();
    }

    /**
     * release memory.
     */
    private void releaseTimer(){
        inheritableThreadLocal.remove();
    }

    /**
     * check query params.
     * @param KSTCQuery
     */
    private void checkQuery(KSTCQuery KSTCQuery){
        //Assert.notNull(KSTCQuery.getLocation(),"please input location.");
        //Assert.checkBetween(KSTCQuery.getLocation().getLongitude(),-180.0,180.0,"wrong lon.");
        //Assert.checkBetween(KSTCQuery.getLocation().getLatitude(),-90.0,90.0,"wrong lat.");
        //Assert.checkBetween(KSTCQuery.getK(),1,20,"wrong k.");
        Assert.checkBetween(KSTCQuery.getEpsilon(),1.0,Double.MAX_VALUE,"wrong epsilon.");
        Assert.checkBetween(KSTCQuery.getMinPts(),2,Integer.MAX_VALUE,"wrong minPts.");
        Assert.checkBetween(KSTCQuery.getMaxDistance(),0,Double.MAX_VALUE,"wrong maxDistance.");
        Assert.notNull(KSTCQuery.getKeywords(),"keywords is null.");
        Assert.isFalse(KSTCQuery.getKeywords().isEmpty(),"keywords is empty.");
    }

    /**
     * kstc search.
     * @param KSTCQuery
     * @return
     */
    @Override
    public List<Set<T>> kstcSearch(KSTCQuery KSTCQuery) {
        checkQuery(KSTCQuery);
        return basic(KSTCQuery);
    }



}
