package com.edu.szu;

import com.edu.szu.api.NamedPoint;
import com.edu.szu.entity.DCPGSParams;
import com.edu.szu.exception.DBSCANClusteringException;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import lombok.Setter;
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

    @Setter
    private DCPGSParams params;

    private final Map<V, List<V>> neighbourMap = new HashMap<>();

    ExecutorService pool = new ThreadPoolExecutor(3, 5, 8, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(6), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    /**
     * Creates a DBSCAN clusterer instance.
     * Upon instantiation, call {@link #performClustering()}
     * to perform the actual clustering.
     *
     * @param inputValues Input values to be clustered
     * @param minNumElements Minimum number of elements to constitute cluster
     * @throws DBSCANClusteringException
     */
    public DCPGS(final Collection<V> inputValues, int minNumElements) throws DBSCANClusteringException {
        setInputValues(inputValues);
        setMinimalNumberOfMembersForCluster(minNumElements);
    }

    /**
     * Sets a collection of input values to be clustered.
     * Repeated call overwrite the original input values.
     *
     * @param collection
     * @throws DBSCANClusteringException
     */
    public void setInputValues(final Collection<V> collection) throws DBSCANClusteringException {
        if (collection == null) {
            throw new DBSCANClusteringException("DBSCAN: List of input values is null.");
        }
        this.inputValues = new ArrayList<V>(collection);
    }

    /**
     * Sets the minimal number of members to consider points of close proximity
     * clustered.
     *
     * @param minimalNumberOfMembers
     */
    public void setMinimalNumberOfMembersForCluster(final int minimalNumberOfMembers) {
        this.minimumNumberOfClusterMembers = minimalNumberOfMembers;
    }

    /**
     * Merges the elements of the right collection to the left one and returns
     * the combination.
     *
     * @param neighbours1 left collection
     * @param neighbours2 right collection
     * @return Modified left collection
     */
    private ArrayList<V> mergeRightToLeftCollection(final Set<V> cache,
                                                    final ArrayList<V> neighbours1,
                                                    final ArrayList<V> neighbours2) {
        for (V tempPt : neighbours2) {
            if (!cache.contains(tempPt)) {
                neighbours1.add(tempPt);
                cache.add(tempPt);
            }
        }
        return neighbours1;
    }

    /**
     * Applies the clustering and returns a collection of clusters (i.e. a list
     * of lists of the respective cluster members).
     *
     * @return
     * @throws DBSCANClusteringException
     */
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
//                int size1 = neighbours.size();
//                neighbours = getNeighbours(p);
//                System.out.println(size1 == neighbours.size());

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

    /**
     * Determines the neighbours of a given input value.
     *
     * @param inputValue Input value for which neighbours are to be determined
     * @return list of neighbours
     * @throws DBSCANClusteringException
     */
    private ArrayList<V> getNeighbours(final V inputValue, RTree<String, V> rTree){
        ArrayList<V> neighbours = new ArrayList<V>();
        Observable<Entry<String, V>> neighbour = rTree.search(inputValue.mbr(), params.getEpsilon());
        neighbour.forEach(n -> neighbours.add(n.geometry()));
        return neighbours;
    }

    private ArrayList<V> getNeighbours(V inputValue){
        return (ArrayList<V>) neighbourMap.get(inputValue);
    }

}
