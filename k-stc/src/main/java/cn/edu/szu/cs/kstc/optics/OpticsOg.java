package cn.edu.szu.cs.kstc.optics;

import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.entity.OpticsRelevantObject;
import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *  OpticsOg
 * @author Whitence
 * @date 2024/4/5 11:12
 * @version 1.0
 */
public class OpticsOg<T extends OpticsRelevantObject> implements TopKSpatialTextualClustersRetrieval<T> {


    private InvertedIndex<T> invertedIndex;

    private IRTree<T> irTree;

    public interface InvertedIndex<U> {

        List<U> getRelevantObjects(List<String> keywords);
    }


    /**
     * IRTree
     *
     * @param <U>
     */
    public interface IRTree<U> {

        /**
         * range query
         *
         * @param keywords
         * @param coordinate
         * @param epsilon
         * @return
         */
        List<U> rangeQuery(List<String> keywords, double[] coordinate, double epsilon);

    }

    public OpticsOg(InvertedIndex<T> invertedIndex, IRTree<T> irTree) {
        this.invertedIndex = invertedIndex;
        this.irTree = irTree;
    }

    @Override
    public List<Set<T>> kstcSearch(KstcQuery query) {

        // Get the relevant objects
        List<T> relevantObjects = invertedIndex.getRelevantObjects(query.getKeywords());

        LinkedHashSet<T> resultSet = new LinkedHashSet<>();

        for (T relevantObject : relevantObjects) {

            if(resultSet.contains(relevantObject)){
                continue;
            }

            List<T> list = irTree.rangeQuery(query.getKeywords(), relevantObject.getCoordinate(), query.getEpsilon());

            if(list.size() >= query.getMinPts()){
                resultSet.add(relevantObject);

            }

        }


        return null;
    }

    private double getCoreDistance(OpticsRelevantObject x, List<OpticsRelevantObject> objects, double eps) {
        return 0;
    }


    private double getReachableDistance(OpticsRelevantObject x, OpticsRelevantObject y) {
        return 0;
    }
}
