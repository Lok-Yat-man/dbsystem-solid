package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.entity.Coordinate;
import cn.edu.szu.cs.entity.Query;
import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.irtree.IRTree;
import cn.edu.szu.cs.ivtidx.InvertedIndex;
import cn.edu.szu.cs.kstc.KSTC;
import cn.edu.szu.cs.util.CommonAlgorithm;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 *  Simple implementation of KSTC algorithm.
 * @author Whitence
 * @date 2023/10/1 11:08
 * @version 1.0
 */

public class SimpleKSTC<T extends RelatedObject> implements KSTC<T> {

    /**
     * iRtree implementation. Used for range query.
     */
    private final IRTree<T> irTree;
    /**
     * Used to obtain related objects according to character strings.
     */
    private final InvertedIndex<T> invertedIndex;

    /**
     * logger
     */
    private static final Log logger = LogFactory.get();
    /**
     * Timer for further performance analysis
     */
    private static final InheritableThreadLocal<TimeInterval> inheritableThreadLocal = new InheritableThreadLocal<>();

    /**
     * Maximum timeout per request
     */
    private static final long DEFAULT_TIMEOUT = 30000L; // 30s

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

        }
    }
    private static LFUCache<String,Object> cache = CacheUtil.newLFUCache(100);

    public SimpleKSTC(IRTree<T> irTree, InvertedIndex<T> invertedIndex) {
        this.irTree = irTree;
        this.invertedIndex = invertedIndex;

    }
    /**
     * cut off precision of cache key.
     * @param query
     * @return
     */
    private String getCacheKey(Query query){
        Coordinate coordinate = Coordinate.create(
                Math.round(query.getLocation().getLongitude() * 1000.0) / 1000.0,
                Math.round(query.getLocation().getLatitude() * 1000.0) / 1000.0
        );
        long maxDist = Math.round(query.getMaxDistance());
        long epsilon = Math.round(query.getEpsilon());
        return coordinate+"_"+query.getKeywords()+"_"+epsilon+"_"+query.getMinPts()+"_"+maxDist;
    }



    private List<Set<T>> basic(Query query){
        logger.info("=> k-stc search start.");
        logger.info("=> k-stc query params: {}.",query.toString());
        getTimer().start("basic");
        List<Set<T>> list = doBasic(query);
        long intervalMs = getTimer().intervalMs("basic");
        releaseTimer();
        logger.info("=> k-stc search time cost:{} ms",intervalMs);
        return list;

    }


    /**
     * Basic algorithm of k-stc
     * @param query
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Set<T>> doBasic(Query query){
        query.setKeywords(
                query.getKeywords().stream().map(String::toLowerCase).collect(Collectors.toList())
        );
        String key = getCacheKey(query);
        if(cache.containsKey(key)){
            Pair<List<Set<T>>, Integer> listIntegerPair = (Pair<List<Set<T>>, Integer>) cache.get(key);
            if(listIntegerPair.getValue()>=query.getK()){
                logger.debug("==> hit cache.");
                return listIntegerPair.getKey().stream().limit(query.getK()).collect(Collectors.toList());
            }
        }

        // sList
        Set<T> noises = new HashSet<>();
        // sort objs ascent by distance
        getTimer().start("timeout");
        PriorityQueue<T> sList = invertedIndex.getSList(
                query.getKeywords(),
                query.getLocation(),
                query.getMaxDistance(),
                Comparator.comparingDouble(a -> CommonAlgorithm.calculateDistance(query.getLocation(), a.getCoordinate()))
        );
        List<Set<T>> rList = new ArrayList<>(query.getK());
        long intervalMs = getTimer().intervalMs("timeout");
        while(!sList.isEmpty() && rList.size()< query.getK() && intervalMs <= DEFAULT_TIMEOUT){
            // get the nearest obj
            T obj = sList.poll();
            Set<T> cluster = getCluster(obj, query, sList,noises);
            if(!cluster.isEmpty()){
                rList.add(cluster);
            }
            intervalMs = getTimer().intervalMs("timeout");
        }
        cache.put(
                key,
                new Pair<>(rList, query.getK())
        );
        // timeout, async
        if(!sList.isEmpty() && rList.size() < query.getK()){
            logger.info("Timeout. Async compute.");
            CompletableFuture.runAsync(
                    ()->continueComputeAsync(
                            rList,
                            sList,
                            query,
                            noises
                    ),
                    ThreadPoolExecutorHolder.threadPool
            );
        }
        return rList;
    }

    private void continueComputeAsync(List<Set<T>> rList,PriorityQueue<T> sList,Query query,Set<T> noises){

        while(!sList.isEmpty() && rList.size()< query.getK()){
            // get the nearest obj
            T obj = sList.poll();
            Set<T> cluster = getCluster(obj, query, sList,noises);
            if(!cluster.isEmpty()){
                rList.add(cluster);
            }
        }
        logger.info("Async compute finished.");
    }

    private Set<T> getCluster(T p,
                                           Query q,
                                           PriorityQueue<T> sList,
                                           Set<T> noises){
        List<T> neighbors = irTree.rangeQuery(q.getKeywords(), p.getCoordinate(),q.getEpsilon());
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
            List<T> neighborsTmp = irTree.rangeQuery(q.getKeywords(), neighbor.getCoordinate(),q.getEpsilon());
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
     * @param query
     */
    private void checkQuery(Query query){
        Assert.notNull(query.getLocation(),"please input location.");
        Assert.checkBetween(query.getLocation().getLongitude(),-180.0,180.0,"wrong lon.");
        Assert.checkBetween(query.getLocation().getLatitude(),-90.0,90.0,"wrong lat.");
        Assert.checkBetween(query.getK(),1,20,"wrong k.");
        Assert.checkBetween(query.getEpsilon(),1.0,Double.MAX_VALUE,"wrong epsilon.");
        Assert.checkBetween(query.getMinPts(),2,Integer.MAX_VALUE,"wrong minPts.");
        Assert.checkBetween(query.getMaxDistance(),0,Double.MAX_VALUE,"wrong maxDistance.");
        Assert.notNull(query.getKeywords(),"keywords is null.");
        Assert.isFalse(query.getKeywords().isEmpty(),"keywords is empty.");
    }

    /**
     * kstc search.
     * @param query
     * @return
     */
    @Override
    public List<Set<T>> kstcSearch(Query query) {
        checkQuery(query);
        return basic(query);
    }



}
