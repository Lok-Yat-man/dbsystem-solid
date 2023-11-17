package com.edu.szu;

import com.edu.szu.api.NamedPoint;
import com.edu.szu.api.PointDistanceCalculator;
import com.edu.szu.entity.DCPGSParams;
import com.edu.szu.exception.DBSCANClusteringException;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import rx.Observable;

import java.util.*;
import java.util.concurrent.*;

public class DCPGS<V extends NamedPoint> {
    /** minimum number of members to consider cluster */
    private int minimumNumberOfClusterMembers = 2;

    /** internal list of input values to be clustered */
    private ArrayList<V> inputValues = null;

    /** index maintaining visited points */
    private final HashSet<V> visitedPoints = new HashSet<V>();

    private final DCPGSParams params;

    private final Map<V, List<V>> neighbourMap = new HashMap<>();

    private final ExecutorService pool;

    private final PointDistanceCalculator<V> pointDistanceCalculator;

    public DCPGS(final Collection<V> inputValues, int minNumElements,
                 PointDistanceCalculator<V> calculator, ExecutorService pool,
                 DCPGSParams params) throws DBSCANClusteringException {
        this.inputValues = (ArrayList<V>) inputValues;
        this.minimumNumberOfClusterMembers = minNumElements;
        this.pointDistanceCalculator = calculator;
        this.pool = pool;
        this.params = params;
    }

    private void mergeRightToLeftCollection(final Set<V> cache,
                                            final ArrayList<V> neighbours1,
                                            final ArrayList<V> neighbours2) {
        for (V tempPt : neighbours2) {
            if (!cache.contains(tempPt)) {
                neighbours1.add(tempPt);
                cache.add(tempPt);
            }
        }
    }

    public ArrayList<ArrayList<V>> performClustering(RTree<String, V> rTree) throws Exception {

        if (inputValues == null) {
            throw new DBSCANClusteringException("DBSCAN: List of input values is null.");
        }

        if (inputValues.isEmpty()) {
            throw new DBSCANClusteringException("DBSCAN: List of input values is empty.");
        }

        if (inputValues.size() < 2) {
            throw new DBSCANClusteringException("DBSCAN: Less than two input values cannot be clustered. Number of input values: " + inputValues.size());
        }

        if (params.getEpsilon() < 0) {
            throw new DBSCANClusteringException("DBSCAN: Maximum distance of input values cannot be negative. Current value: " + params.getEpsilon());
        }

        if (minimumNumberOfClusterMembers < 2) {
            throw new DBSCANClusteringException("DBSCAN: Clusters with less than 2 members don't make sense. Current value: " + minimumNumberOfClusterMembers);
        }

        visitedPoints.clear();
        ArrayList<ArrayList<V>> resultList = new ArrayList<>();
        ArrayList<V> neighbours;
        int index = 0;
        getAllNeighbours(rTree);
        while (index < inputValues.size()) {
            V p = inputValues.get(index);
            if (!visitedPoints.contains(p)) {
                visitedPoints.add(p);
                neighbours = getNeighbours(p);

                if (neighbours.size() >= minimumNumberOfClusterMembers) {
                    Set<V> cache = new HashSet<>(neighbours);
                    int ind = 0;
                    while (neighbours.size() > ind) {
                        V r = neighbours.get(ind);
                        if (!visitedPoints.contains(r)) {
                            visitedPoints.add(r);
                            ArrayList<V> individualNeighbours = getNeighbours(r);
                            if (individualNeighbours.size() >= minimumNumberOfClusterMembers) {
                                mergeRightToLeftCollection(
                                        cache,
                                        neighbours,
                                        individualNeighbours);
                            }
                        }
                        ind++;
                    }
                    resultList.add(neighbours);
                }
            }
            index++;
        }
        return resultList;
    }

    public void getAllNeighbours(RTree<String, V> rTree) throws Exception {
        int numThread = 4;
        int gap = inputValues.size()/numThread;
        List<Future<Map<V, List<V>>>> futures = new ArrayList<>();
        for (int i = 0; i < numThread; i++) {
            int startIndex = i * gap;
            int endIndex;
            if(i == numThread - 1){
                endIndex = inputValues.size() - 1;
            } else {
                endIndex = (i + 1) * gap - 1;
            }
            futures.add(pool.submit(()-> getAllNeighbours(rTree,startIndex,endIndex)));
        }
        for (Future<Map<V, List<V>>> future : futures) {
            Map<V, List<V>> vListMap = future.get();
            this.neighbourMap.putAll(vListMap);
        }
    }

    private Map<V, List<V>> getAllNeighbours(RTree<String, V> rTree, int startIndex, int endIndex){
        Map<V, List<V>> neighboursMap = new HashMap<>();
        int index = startIndex;
        while (index <= endIndex) {
            V p = inputValues.get(index);
            ArrayList<V> neighbours = getNeighbours(p,rTree);
            neighboursMap.put(p,neighbours);
            index++;
        }
        return neighboursMap;
    }

    private ArrayList<V> getNeighbours(final V inputValue, RTree<String, V> rTree){
        ArrayList<V> neighbours = new ArrayList<V>();
        Observable<Entry<String, V>> neighbour = rTree.search(inputValue.mbr(), params.getMaxD());
        neighbour.forEach(n -> {
            V checkIn = (V) n.geometry();
            if(pointDistanceCalculator.calculateDistance(inputValue, checkIn)
                    <= params.getEpsilon()){
                neighbours.add(checkIn);
            }
        });
        return neighbours;
    }

    private ArrayList<V> getNeighbours(V inputValue){
        return (ArrayList<V>) neighbourMap.get(inputValue);
    }

}
