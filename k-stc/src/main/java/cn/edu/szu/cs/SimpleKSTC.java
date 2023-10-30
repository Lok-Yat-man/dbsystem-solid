package cn.edu.szu.cs;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *  Simple implementation of KSTC algorithm.
 * @author Whitence
 * @date 2023/10/1 11:08
 * @version 1.0
 */

public class SimpleKSTC implements KSTC{

    /**
     * iRtree implementation. Used for range query.
     */
    private final IRTree irTree;
    /**
     * Used to obtain related objects according to character strings.
     */
    private final InvertedIndex<RelevantObject> invertedIndex;

    private static LFUCache<String,List<KstcCluster>> cache = CacheUtil.newLFUCache(100);

    public SimpleKSTC(){

        IRelevantObjectService relevantObjectService = new DefaultRelevantObjectServiceImpl();

        invertedIndex = new DefaultLeafInvertedIndex(relevantObjectService);

        irTree = new SimpleIRTree(relevantObjectService);

    }



    private List<Set<RelevantObject>> basic(Query query){
        return doBasic(query, irTree, invertedIndex);
    }

    /**
     * Basic algorithm of k-stc
     * @param query
     * @param irTree
     * @param invertedIndex
     * @return
     */
    private List<Set<RelevantObject>> doBasic(Query query,IRTree irTree,InvertedIndex<RelevantObject> invertedIndex){

        // sList
        Set<RelevantObject> noises = new HashSet<>();
        // sort objs ascent by distance
        PriorityQueue<RelevantObject> sList = invertedIndex.getSList(
                query.getKeywords(),
                Comparator.comparingDouble(a -> CommonAlgorithm.calculateDistance(query.getLocation(), a.getCoordinate()))
        );

        List<Set<RelevantObject>> rList = new LinkedList<>();

        while(!sList.isEmpty() && rList.size()< query.getK()){
            // get the nearest obj
            RelevantObject obj = sList.peek();

            Set<RelevantObject> cluster = getCluster(obj, query, irTree, sList,noises);
            if(!cluster.isEmpty()){

                rList.add(cluster);
            }
        }

        return rList;

    }


    private Set<RelevantObject> getCluster(RelevantObject p,
                                           Query q,
                                           IRTree irTree,
                                           PriorityQueue<RelevantObject> sList,
                                           Set<RelevantObject> noises){

        List<RelevantObject> neighbors = irTree.rangeQuery(q, p);

        if(neighbors.size() < q.getMinPts()){
            sList.remove(p);
            // mark p
            noises.add(p);

            return Collections.emptySet();
        }

        Set<RelevantObject> result = new HashSet<>(neighbors);

        neighbors.forEach(sList::remove);

        neighbors.remove(p);

        while(!neighbors.isEmpty()){

            RelevantObject neighbor = neighbors.remove(0);

            List<RelevantObject> neighborsTmp = irTree.rangeQuery(q, neighbor);

            if(neighborsTmp.size() >= q.getMinPts()){

                for (RelevantObject obj : neighborsTmp) {

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
    public List<KstcCluster> kstcSearch(Query query) {

        if(cache.containsKey(query.toString())){
            return cache.get(query.toString());
        }

        KstcCluster.resetId();
        List<KstcCluster> kstcClusters = basic(query).stream()
                .map(KstcCluster::create)
                .collect(Collectors.toList());

        cache.put(query.toString(),kstcClusters);

        return kstcClusters;
    }
}
