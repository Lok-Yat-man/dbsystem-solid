package cn.edu.szu.cs;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *  Simple implementation of KSTC algorithm.
 * @author Whitence
 * @date 2023/10/1 11:08
 * @version 1.0
 */

public class SimpleKSTC<T extends RelatedObject> implements KSTC<T>{

    /**
     * iRtree implementation. Used for range query.
     */
    private final IRTree<T> irTree;
    /**
     * Used to obtain related objects according to character strings.
     */
    private final InvertedIndex<T> invertedIndex;

    private LFUCache<String,List<Set<T>>> cache = CacheUtil.newLFUCache(100);



    public SimpleKSTC(IRTree<T> irTree, InvertedIndex<T> invertedIndex) {
        this.irTree = irTree;
        this.invertedIndex = invertedIndex;
    }

    private String preHandleQuery(Query query){

        Coordinate coordinate = Coordinate.create(
                Math.round(query.getLocation().getLongitude() * 1000.0) / 1000.0,
                Math.round(query.getLocation().getLatitude() * 1000.0) / 1000.0
        );
        long maxDist = Math.round(query.getMaxDistance());
        long epsilon = Math.round(query.getEpsilon());

        return query.toBuilder()
                .location(coordinate)
                .epsilon(epsilon)
                .maxDistance(maxDist)
                .build().toString();

    }

    @SuppressWarnings("unchecked")
    private List<Set<T>> basic(Query query){

        //List<String> keys = invertedIndex.keys();
        //
        //List<String> newKeywords = new ArrayList<>();
        //
        //for (String kwd : query.getKeywords()) {
        //
        //    for (String key : keys) {
        //        if(key.contains(kwd.toLowerCase())){
        //            newKeywords.add(key);
        //        }
        //    }
        //}
        //query.setKeywords(newKeywords);

        query.setKeywords(
                query.getKeywords().stream().map(String::toLowerCase).collect(Collectors.toList())
        );

        String cacheKey = preHandleQuery(query);
        //if(cache.containsKey(cacheKey)){
        //    return cache.get(cacheKey);
        //}
        List<Set<T>> list = doBasic(query, irTree, invertedIndex);
        //cache.put(cacheKey,list);
        return list;

    }

    /**
     * Basic algorithm of k-stc
     * @param query
     * @param irTree
     * @param invertedIndex
     * @return
     */
    private List<Set<T>> doBasic(Query query,IRTree<T> irTree,InvertedIndex<T> invertedIndex){
        // sList
        Set<T> noises = new HashSet<>();
        // sort objs ascent by distance
        PriorityQueue<T> sList = invertedIndex.getSList(
                query.getKeywords(),
                query.getLocation(),
                query.getMaxDistance(),
                Comparator.comparingDouble(a -> CommonAlgorithm.calculateDistance(query.getLocation(), a.getCoordinate()))
        );

        List<Set<T>> rList = new LinkedList<>();

        while(!sList.isEmpty() && rList.size()< query.getK()){
            // get the nearest obj
            T obj = sList.peek();

            Set<T> cluster = getCluster(obj, query, irTree, sList,noises);
            if(!cluster.isEmpty()){

                rList.add(cluster);
            }
        }
        return rList;


    }


    private Set<T> getCluster(T p,
                                           Query q,
                                           IRTree<T> irTree,
                                           PriorityQueue<T> sList,
                                           Set<T> noises){

        List<T> neighbors = irTree.rangeQuery(q.getKeywords(), p.getCoordinate(),q.getEpsilon());

        if(neighbors.size() < q.getMinPts()){
            sList.remove(p);
            // mark p
            noises.add(p);

            return Collections.emptySet();
        }

        Set<T> result = new HashSet<>(neighbors);

        neighbors.forEach(sList::remove);

        neighbors.remove(p);

        while(!neighbors.isEmpty()){

            T neighbor = neighbors.remove(0);

            List<T> neighborsTmp = irTree.rangeQuery(q.getKeywords(), neighbor.getCoordinate(),q.getEpsilon());

            if(neighborsTmp.size() >= q.getMinPts()){

                for (T obj : neighborsTmp) {

                    if(noises.contains(obj)){
                        result.add(obj);
                    }else if(!result.contains(obj)){
                        result.add(obj);

                        sList.remove(obj);
                        neighbors.add(obj);

                    }

                }
            }

        }

        return result;
    }

    @Override
    public List<Set<T>> kstcSearch(Query query) {
        Assert.notNull(query.getLocation(),"please input location.");
        Assert.checkBetween(query.getLocation().getLongitude(),-180.0,180.0,"wrong lon.");
        Assert.checkBetween(query.getLocation().getLatitude(),-90.0,90.0,"wrong lat.");
        Assert.checkBetween(query.getK(),1,100,"wrong k.");
        Assert.checkBetween(query.getEpsilon(),1.0,Double.MAX_VALUE,"wrong epsilon.");
        Assert.checkBetween(query.getMinPts(),0,Integer.MAX_VALUE,"wrong minPts.");
        Assert.checkBetween(query.getMaxDistance(),0,Double.MAX_VALUE,"wrong maxDistance.");
        Assert.notNull(query.getKeywords(),"keywords is null.");
        Assert.isFalse(query.getKeywords().isEmpty(),"keywords is empty.");


        return basic(query);
    }

    public static void main(String[] args) {

        System.out.println(Math.round(1.6111));

    }
}
