package com.edu.szu;

import com.edu.szu.api.NamedPoint;
import com.edu.szu.entity.DCPGSParams;
import com.edu.szu.exception.DBSCANClusteringException;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import lombok.Setter;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class DCPGS<V extends NamedPoint> {
    /** minimum number of members to consider cluster */
    private int minimumNumberOfClusterMembers = 2;

    /** internal list of input values to be clustered */
    private ArrayList<V> inputValues = null;

    /** index maintaining visited points */
    private final HashSet<V> visitedPoints = new HashSet<V>();

    @Setter
    private DCPGSParams params;

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

    /**
     * Merges the elements of the right collection to the left one and returns
     * the combination.
     *
     * @param neighbours1 left collection
     * @param neighbours2 right collection
     * @return Modified left collection
     */
    private ArrayList<V> mergeRightToLeftCollection(final ArrayList<V> neighbours1,
                                                    final ArrayList<V> neighbours2) {
        for (V tempPt : neighbours2) {
            if (!neighbours1.contains(tempPt)) {
                neighbours1.add(tempPt);
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
    public ArrayList<ArrayList<V>> performClustering(RTree<String, V> rTree) throws DBSCANClusteringException {

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

        ArrayList<ArrayList<V>> resultList = new ArrayList<ArrayList<V>>();
        visitedPoints.clear();

        ArrayList<V> neighbours;
        int index = 0;

        while (inputValues.size() > index) {
            V p = inputValues.get(index);
            if (!visitedPoints.contains(p)) {
                visitedPoints.add(p);
                neighbours = getNeighbours(p,rTree);

                if (neighbours.size() >= minimumNumberOfClusterMembers) {
                    int ind = 0;
                    while (neighbours.size() > ind) {
                        V r = neighbours.get(ind);
                        if (!visitedPoints.contains(r)) {
                            visitedPoints.add(r);
                            ArrayList<V> individualNeighbours = getNeighbours(r,rTree);
                            if (individualNeighbours.size() >= minimumNumberOfClusterMembers) {
                                mergeRightToLeftCollection(
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
}
