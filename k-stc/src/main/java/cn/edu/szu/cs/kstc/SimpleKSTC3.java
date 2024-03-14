//package cn.edu.szu.cs.kstc;
//
//import cn.edu.szu.cs.entity.Coordinate;
//import cn.edu.szu.cs.entity.GeoPointDouble;
//import cn.edu.szu.cs.entity.Query;
//import cn.edu.szu.cs.entity.RelatedObject;
//import cn.edu.szu.cs.service.IRelatedObjectService;
//import cn.edu.szu.cs.util.CommonAlgorithm;
//import cn.hutool.cache.CacheUtil;
//import cn.hutool.cache.impl.LFUCache;
//import cn.hutool.cache.impl.LRUCache;
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.date.TimeInterval;
//import cn.hutool.core.lang.Assert;
//import cn.hutool.core.lang.Pair;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.log.Log;
//import cn.hutool.log.LogFactory;
//import com.github.davidmoten.rtree.Entries;
//import com.github.davidmoten.rtree.Entry;
//import com.github.davidmoten.rtree.Node;
//import com.github.davidmoten.rtree.RTree;
//import com.github.davidmoten.rtree.internal.LeafDefault;
//import com.github.davidmoten.rtree.internal.NonLeafDefault;
//
//import java.lang.ref.SoftReference;
//import java.util.*;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
///**
// *  Simple implementation of KSTC algorithm.
// * @author Whitence
// * @date 2023/10/1 11:08
// * @version 1.0
// */
//
//public class SimpleKSTC3<T extends RelatedObject> implements KSTC<T> {
//    // Some tools
//    /**
//     * logger
//     */
//    private static final Log logger = LogFactory.get();
//    /**
//     * Timer for further performance analysis
//     */
//    private static final InheritableThreadLocal<TimeInterval> inheritableThreadLocal = new InheritableThreadLocal<>();
//
//    /**
//     * Maximum timeout per request.
//     */
//    private static final long DEFAULT_TIMEOUT = 30_000L;
//    /**
//     * Thread pool for asynchronous computing
//     */
//    private static class ThreadPoolExecutorHolder{
//        public static ThreadPoolExecutor threadPool = null;
//        static {
//            int processors = Runtime.getRuntime().availableProcessors();
//            threadPool=new ThreadPoolExecutor(
//                    processors+1,
//                    2*processors,
//                    30,
//                    TimeUnit.SECONDS,
//                    new ArrayBlockingQueue<>(10),
//                    new ThreadPoolExecutor.CallerRunsPolicy()
//            );
//            logger.debug("Thread pool initialized successfully.");
//        }
//    }
//
//    /**
//     * rTree is used to store the spatial information of related objects.
//     */
//    private RTree<RelatedObject,GeoPointDouble> rTree;
//    /**
//     * Preserve the extensibility of the way to obtain related objects.
//     */
//    private IRelatedObjectService relatedObjectService;
//    /**
//     * The main cache is used to cache results. Using soft references ensures that there will be no out-of-memory errors.
//     */
//    private static volatile SoftReference<LFUCache<String,Object>> mainCache = null;
//    private LFUCache<String,Object> mainCache(){
//        if(mainCache == null || mainCache.get() == null){
//            synchronized (SimpleKSTC3.class){
//                if(mainCache == null){
//                    mainCache = new SoftReference<>(CacheUtil.newLFUCache(128));
//                    logger.debug("Main cache initialized successfully.");
//                }
//            }
//        }
//        return mainCache.get();
//    }
//
//    public static class Trie{
//
//        /**
//         * relatedObject cache is used to cache the results of inverted index.
//         */
//        private static volatile SoftReference<LRUCache<String,Set<RelatedObject>>> relatedObjectCache = null;
//        private LRUCache<String,Set<RelatedObject>> relatedObjectCache(){
//            if(relatedObjectCache == null){
//                synchronized (SimpleKSTC3.class){
//                    if(relatedObjectCache == null){
//                        relatedObjectCache=new SoftReference<>(CacheUtil.newLRUCache(32,DEFAULT_TIMEOUT));
//                        logger.debug("Inverted index cache initialized successfully.");
//                    }
//                }
//            }
//            return relatedObjectCache.get();
//        }
//
//
//        /**
//         * node cache is used to cache the results of inverted index.
//         */
//        private static volatile SoftReference<LRUCache<String,Set<Node<RelatedObject, GeoPointDouble>>>> nodeCache = null;
//        private LRUCache<String,Set<Node<RelatedObject, GeoPointDouble>>> nodeCache(){
//            if(nodeCache == null){
//                synchronized (SimpleKSTC3.class){
//                    if(nodeCache == null){
//                        nodeCache=new SoftReference<>(CacheUtil.newLRUCache(32,DEFAULT_TIMEOUT));
//                        logger.debug("Inverted index cache initialized successfully.");
//                    }
//                }
//            }
//            return nodeCache.get();
//        }
//
//        private boolean isWord;
//        private Map<Character, Trie> children;
//        private Set<RelatedObject> objs;
//        private Set<Node<RelatedObject, GeoPointDouble>> nodes;
//
//        public void putRelatedObject(String keyword, RelatedObject object){
//            Trie cur=this;
//            for (char ch : keyword.toLowerCase().toCharArray()) {
//                if(cur.children == null){
//                    cur.children = new HashMap<>(128);
//                }
//                cur.children.putIfAbsent(ch,new Trie());
//                cur=cur.children.get(ch);
//            }
//            cur.isWord=true;
//            if(cur.objs == null){
//                cur.objs=new HashSet<>();
//            }
//            cur.objs.add(object);
//        }
//        public void putNode(String keyword,Node<RelatedObject, GeoPointDouble> node){
//            Trie cur=this;
//            for (char ch : keyword.toLowerCase().toCharArray()) {
//                if(cur.children == null){
//                    cur.children = new HashMap<>(128);
//                }
//                cur.children.putIfAbsent(ch,new Trie());
//                cur=cur.children.get(ch);
//            }
//            cur.isWord=true;
//            if(cur.nodes == null){
//                cur.nodes=new HashSet<>();
//            }
//            cur.nodes.add(node);
//        }
//
//        public Set<RelatedObject> searchRelatedObject(String keyword){
//            if(StrUtil.isBlank(keyword)){
//                return Collections.emptySet();
//            }
//            if(relatedObjectCache().containsKey(keyword)){
//                return relatedObjectCache().get(keyword);
//            }
//            Trie cur = this;
//            for (char ch : keyword.toCharArray()) {
//                if(cur.children == null){
//                    return Collections.emptySet();
//                }
//                cur=cur.children.get(ch);
//            }
//            Set<RelatedObject> result = new HashSet<>();
//            Queue<Trie> queue = new LinkedList<>();
//            queue.offer(cur);
//            while (!queue.isEmpty()){
//                Trie node = queue.poll();
//                if(node.isWord){
//                    result.addAll(node.objs);
//                }
//                if(node.children != null){
//                    queue.addAll(node.children.values());
//                }
//            }
//            relatedObjectCache().put(keyword,result);
//            return result;
//        }
//
//        public Set<Node<RelatedObject, GeoPointDouble>> searchNode(String keyword){
//            if(StrUtil.isBlank(keyword)){
//                return Collections.emptySet();
//            }
//            if(nodeCache().containsKey(keyword)){
//                return nodeCache().get(keyword);
//            }
//            Trie cur = this;
//            for (char ch : keyword.toCharArray()) {
//                if(cur.children == null){
//                    return Collections.emptySet();
//                }
//                cur=cur.children.get(ch);
//            }
//            Set<Node<RelatedObject, GeoPointDouble>> result = new HashSet<>();
//            Queue<Trie> queue = new LinkedList<>();
//            queue.offer(cur);
//            while (!queue.isEmpty()){
//                Trie node = queue.poll();
//                if(node.isWord){
//                    result.addAll(node.nodes);
//                }
//                if(node.children != null){
//                    queue.addAll(node.children.values());
//                }
//            }
//            nodeCache().put(keyword,result);
//            return result;
//        }
//
//    }
//
//
//
//
//    Trie trie = new Trie();
//    private Map<Node<RelatedObject, GeoPointDouble>,Trie> leafNodeTrieMap = new HashMap<>();
//    private Map<Node<RelatedObject, GeoPointDouble>,Trie> nonLeafNodeTrieMap = new HashMap<>();
//
//    public SimpleKSTC3(IRelatedObjectService relatedObjectService) {
//        Assert.notNull(relatedObjectService,"relatedObjectService is null.");
//        this.relatedObjectService=relatedObjectService;
//        List<RelatedObject> objects = relatedObjectService.getAll();
//        Assert.isTrue(objects instanceof RandomAccess,"If RandomAccess is not supported, the construction of rtree will become very slow.");
//        buildrTreeAndTrieInvertedIndex(objects);
//        rTree.root().ifPresent(this::buildTrieInvertedIndexForNodes);
//    }
//    private void buildrTreeAndTrieInvertedIndex(List<RelatedObject> objects){
//        for (RelatedObject object : objects) {
//            for (String label : object.getLabels()) {
//                trie.putRelatedObject(label,object);
//            }
//        }
//        logger.debug("Trie initialized successfully.");
//        List<Entry<RelatedObject, GeoPointDouble>> entryList = objects.stream()
//                .map(object -> Entries.entry(object, GeoPointDouble.create(object.getCoordinate().getLongitude(), object.getCoordinate().getLatitude())))
//                .collect(Collectors.toList());
//        rTree = RTree
//                .star()
//                .maxChildren(30)
//                .create(entryList);
//        logger.debug("Rtree initialized successfully.");
//    }
//    private void buildTrieInvertedIndexForNodes(Node<RelatedObject, GeoPointDouble> node){
//        doBuildTrieInvertedIndex(node);
//        logger.debug("Trie inverted index initialized successfully.");
//    }
//
//    @SuppressWarnings("all")
//    private Set<String> doBuildTrieInvertedIndex(Node<RelatedObject, GeoPointDouble> node){
//        Set<String> result = new HashSet<>();
//        if(node instanceof LeafDefault){
//            LeafDefault<RelatedObject,GeoPointDouble> leafDefault = (LeafDefault<RelatedObject,GeoPointDouble>) node;
//            for (Entry<RelatedObject,GeoPointDouble> entry : leafDefault.entries()) {
//                result.addAll(entry.value().getLabels());
//            }
//        }
//        if(node instanceof NonLeafDefault){
//            NonLeafDefault<RelatedObject,GeoPointDouble> nonLeafDefault = (NonLeafDefault<RelatedObject,GeoPointDouble>) node;
//            for (Node<RelatedObject, GeoPointDouble> child : nonLeafDefault.children()) {
//                Set<String> childResult = doBuildTrieInvertedIndex(child);
//                for (String s : childResult) {
//                    trie.putNode(s,child);
//                }
//                result.addAll(childResult);
//            }
//        }
//        return result;
//    }
//
//
//    @SuppressWarnings("unchecked")
//    private PriorityQueue<T> getSList(List<String> keywords,Coordinate coordinate,double maxDist,Comparator<T> comparator){
//        return searchRelatedObjects(keywords)
//                .stream()
//                .filter(object -> CommonAlgorithm.calculateDistance(coordinate,object.getCoordinate()) < maxDist)
//                .map(object -> (T)object)
//                .collect(Collectors.toCollection(()->new PriorityQueue<>(comparator)));
//
//    }
//
//    private Set<Node<RelatedObject,GeoPointDouble>> searchNodes(List<String> keywords){
//        if(keywords == null || keywords.isEmpty()){
//            return Collections.emptySet();
//        }
//
//        Set<Node<RelatedObject,GeoPointDouble>> result = trie.searchNode(keywords.get(0));
//        for (int i = 1; i < keywords.size(); i++) {
//            Set<Node<RelatedObject, GeoPointDouble>> nodes = trie.searchNode(keywords.get(i));
//            if(CollUtil.isEmpty(nodes)){
//                continue;
//            }
//            result.retainAll(nodes);
//        }
//        return result;
//    }
//
//    private Set<RelatedObject> searchRelatedObjects(List<String> keywords){
//        if(keywords == null || keywords.isEmpty()){
//            return Collections.emptySet();
//        }
//        Set<RelatedObject> res = trie.searchRelatedObject(keywords.get(0));
//        for (int i = 1; i < keywords.size(); i++) {
//            Set<RelatedObject> relatedObjects = trie.searchRelatedObject(keywords.get(i));
//            if(CollUtil.isEmpty(relatedObjects)){
//                continue;
//            }
//            res.retainAll(relatedObjects);
//        }
//        return res;
//    }
//
//    @SuppressWarnings("unchecked")
//    private List<T> rangeQuery(List<String> keywords,Coordinate coordinate,double epsilon){
//        Queue<Node<RelatedObject,GeoPointDouble>> queue = new LinkedList<>();
//        rTree.root().ifPresent(queue::add);
//        GeoPointDouble currentPosition = GeoPointDouble.create(coordinate.getLongitude(), coordinate.getLatitude());
//        Set<Node<RelatedObject, GeoPointDouble>> nodes = searchNodes(keywords);
//        List<T> result = new ArrayList<>();
//        while(!queue.isEmpty()){
//            int size = queue.size();
//            for (int i = 0; i < size; i++) {
//                Node<RelatedObject,GeoPointDouble> node = queue.poll();
//                if(node.geometry().distance(currentPosition) > epsilon){
//                    continue;
//                }
//                if(node instanceof LeafDefault){
//
//
//                }
//                if(node instanceof NonLeafDefault){
//                    NonLeafDefault<RelatedObject,GeoPointDouble> nonLeafDefault = (NonLeafDefault<RelatedObject, GeoPointDouble>)node;
//                    nonLeafDefault.children()
//                            .stream()
//                            .filter(nodes::contains)
//                            .forEach(queue::add);
//                }
//            }
//        }
//        return result;
//    }
//
//    /**
//     * cut off precision of cache key.
//     * @param query
//     * @return
//     */
//    private String getCacheKey(Query query){
//        Coordinate coordinate = Coordinate.create(
//                Math.round(query.getLocation().getLongitude() * 1000.0) / 1000.0,
//                Math.round(query.getLocation().getLatitude() * 1000.0) / 1000.0
//        );
//        long maxDist = Math.round(query.getMaxDistance());
//        long epsilon = Math.round(query.getEpsilon());
//        return coordinate+"_"+query.getKeywords()+"_"+epsilon+"_"+query.getMinPts()+"_"+maxDist;
//    }
//
//
//
//    private List<Set<T>> basic(Query query){
//        logger.info("=> k-stc search start.");
//        logger.info("=> k-stc query params: {}.",query.toString());
//        getTimer().start("basic");
//        List<Set<T>> list = doBasic(query);
//        long intervalMs = getTimer().intervalMs("basic");
//        releaseTimer();
//        logger.info("=> k-stc search time cost:{} ms",intervalMs);
//        return list;
//
//    }
//
//
//    /**
//     * Basic algorithm of k-stc
//     * @param query
//     * @return
//     */
//    @SuppressWarnings("unchecked")
//    private List<Set<T>> doBasic(Query query){
//        query.setKeywords(
//                query.getKeywords().stream().map(String::toLowerCase).collect(Collectors.toList())
//        );
//
//        String key = getCacheKey(query);
//        if(mainCache().containsKey(key)){
//            Pair<List<Set<T>>, Integer> listIntegerPair = (Pair<List<Set<T>>, Integer>) mainCache().get(key);
//            if(listIntegerPair.getValue()>=query.getK()){
//                logger.debug("==> hit cache.");
//                return listIntegerPair.getKey().stream().limit(query.getK()).collect(Collectors.toList());
//            }
//        }
//
//        // sList
//        Set<T> noises = new HashSet<>();
//        // sort objs ascent by distance
//        getTimer().start("timeout");
//        PriorityQueue<T> sList = getSList(
//                query.getKeywords(),
//                query.getLocation(),
//                query.getMaxDistance(),
//                Comparator.comparingDouble(a -> CommonAlgorithm.calculateDistance(query.getLocation(), a.getCoordinate()))
//        );
//        logger.info("==> getsList time cost:{}",getTimer().intervalMs("timeout"));
//        List<Set<T>> rList = new ArrayList<>(query.getK());
//        long intervalMs = getTimer().intervalMs("timeout");
//        while(!sList.isEmpty() && rList.size()< query.getK() && intervalMs <= DEFAULT_TIMEOUT){
//            // get the nearest obj
//            T obj = sList.poll();
//            Set<T> cluster = getCluster(obj, query, sList,noises);
//            if(!cluster.isEmpty()){
//                intervalMs = getTimer().intervalMs("timeout");
//                rList.add(cluster);
//                logger.info("==> cluster {}, time cost:{}",rList.size(),getTimer().intervalMs("timeout"));
//                getTimer().intervalRestart();
//            }
//        }
//        mainCache().put(
//                key,
//                new Pair<>(rList, query.getK())
//        );
//        // timeout, async
//        if(!sList.isEmpty() && rList.size() < query.getK()){
//            logger.info("Timeout. Async compute.");
//            CompletableFuture.runAsync(
//                    ()->continueComputeAsync(
//                            rList,
//                            sList,
//                            query,
//                            noises,
//                            key
//                    ),
//                    ThreadPoolExecutorHolder.threadPool
//            );
//        }
//        return rList;
//    }
//
//    private void continueComputeAsync(List<Set<T>> rList,PriorityQueue<T> sList,Query query,Set<T> noises,String key){
//        while(!sList.isEmpty() && rList.size()< query.getK()){
//            // get the nearest obj
//            T obj = sList.poll();
//            Set<T> cluster = getCluster(obj, query, sList,noises);
//            if(!cluster.isEmpty()){
//                rList.add(cluster);
//            }
//        }
//        mainCache().put(key,new Pair<>(rList,query.getK()));
//        logger.info("Async compute finished.");
//    }
//
//    private Set<T> getCluster(T p,
//                                           Query q,
//                                           PriorityQueue<T> sList,
//                                           Set<T> noises){
//        List<T> neighbors = rangeQuery(q.getKeywords(), p.getCoordinate(),q.getEpsilon());
//        if(neighbors.size() < q.getMinPts()){
//            // mark p
//            noises.add(p);
//            return Collections.emptySet();
//        }
//        Set<T> result = new HashSet<>(neighbors);
//        neighbors.remove(p);
//        while(!neighbors.isEmpty()){
//            T neighbor = neighbors.remove(0);
//            sList.remove(neighbor);
//            List<T> neighborsTmp = rangeQuery(q.getKeywords(), neighbor.getCoordinate(),q.getEpsilon());
//            if(neighborsTmp.size() >= q.getMinPts()){
//                for (T obj : neighborsTmp) {
//                    if(noises.contains(obj)){
//                        result.add(obj);
//                    }else if(!result.contains(obj)){
//                        result.add(obj);
//                        neighbors.add(obj);
//                    }
//                }
//            }
//        }
//        return result;
//    }
//
//    /**
//     * every thread has its own timer.
//     * @return
//     */
//    private TimeInterval getTimer(){
//        if(inheritableThreadLocal.get() == null){
//            inheritableThreadLocal.set(new TimeInterval());
//        }
//        return inheritableThreadLocal.get();
//    }
//
//    /**
//     * release memory.
//     */
//    private void releaseTimer(){
//        inheritableThreadLocal.remove();
//    }
//
//    /**
//     * check query params.
//     * @param query
//     */
//    private void checkQuery(Query query){
//        Assert.notNull(query.getLocation(),"please input location.");
//        Assert.checkBetween(query.getLocation().getLongitude(),-180.0,180.0,"wrong lon.");
//        Assert.checkBetween(query.getLocation().getLatitude(),-90.0,90.0,"wrong lat.");
//        Assert.checkBetween(query.getK(),1,20,"wrong k.");
//        Assert.checkBetween(query.getEpsilon(),1.0,Double.MAX_VALUE,"wrong epsilon.");
//        Assert.checkBetween(query.getMinPts(),2,Integer.MAX_VALUE,"wrong minPts.");
//        Assert.checkBetween(query.getMaxDistance(),0,Double.MAX_VALUE,"wrong maxDistance.");
//        Assert.notNull(query.getKeywords(),"keywords is null.");
//        Assert.isFalse(query.getKeywords().isEmpty(),"keywords is empty.");
//    }
//
//    /**
//     * kstc search.
//     * @param query
//     * @return
//     */
//    @Override
//    public List<Set<T>> kstcSearch(Query query) {
//        checkQuery(query);
//        return basic(query);
//    }
//
//
//    public static void main(String[] args) {
//
//        List<Long> longs = Arrays.asList(
//                5L,5L,4L, 4L, 3L, 2L, 1L
//        );
//
//        TreeSet<Long> treeSet = new TreeSet<>();
//
//        Comparator<? super Long> comparator = treeSet.comparator();
//
//    }
//}
