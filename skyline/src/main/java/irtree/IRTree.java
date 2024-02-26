package irtree;

import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.HasGeometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.internal.GeometryUtil;
import com.github.davidmoten.rtree.internal.LeafDefault;
import com.github.davidmoten.rtree.internal.NonLeafDefault;
import entity.*;
import lombok.Getter;
import lombok.Setter;
import service.IRelevantObjectService;


import java.io.InputStream;
import java.util.*;

/**
 * IRtree
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/10/8 16:39
 */
@Getter
@Setter
public final class IRTree {
    /**
     * rTree
     */
    private RTree<String, Geometry> rTree;
    /**
     * Record the relationship between non-leaf nodes and IF
     */
    private Map<Node<String, Geometry>, Map<String, List<NodePair>>> nodeInvertedIndexMap;
    /**
     * Record the relationship between leaf nodes and IF
     */
    private Map<Node<String, Geometry>, Map<String, List<StringPair>>> leafInvertedIndexMap;

    private IRelevantObjectService relevantObjectService;


    public IRTree(IRelevantObjectService relevantObjectService) {

        try (
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("rtree.txt");
        ) {
            // deserialize rTree
            RTree<String, Geometry> rTree = Serializers.flatBuffers().utf8().read(
                    inputStream,
                    inputStream.available(),
                    InternalStructure.DEFAULT
            );

            assert relevantObjectService != null && rTree != null && rTree.root().isPresent();

            this.rTree = rTree;
            this.relevantObjectService = relevantObjectService;

            nodeInvertedIndexMap = new HashMap<>();
            leafInvertedIndexMap = new HashMap<>();

            buildIRtree(rTree.root().get());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public IRTree(RTree<String, Geometry> rTree, IRelevantObjectService relevantObjectService) {
        assert relevantObjectService != null;
        assert rTree != null && rTree.root().isPresent();

        this.rTree = rTree;
        this.relevantObjectService = relevantObjectService;

        nodeInvertedIndexMap = new HashMap<>();
        leafInvertedIndexMap = new HashMap<>();

        buildIRtree(rTree.root().get());

    }

    private Map<String, Double> buildIRtree(Node<String, Geometry> curNode) {

        // Processing leaf nodes
        if (curNode instanceof LeafDefault) {
            LeafDefault leaf = (LeafDefault) curNode;
            List<Entry<String, Point>> entries = leaf.entries();
            Map<String, List<StringPair>> leafIF = new HashMap<>(leaf.count());
            Map<String, Double> ans = new HashMap<>(leaf.count());
            for (Entry<String, Point> entry : entries) {
                String id = entry.value();
                relevantObjectService.getWeightsById(id).forEach(
                        (k, v) -> {
                            // 计算当前节点的IF
                            leafIF.putIfAbsent(k, new ArrayList<>());
                            leafIF.get(k).add(StringPair.create(id, v));
                            // 计算当前节点每个字符串的最大权重
                            ans.put(k, Double.max(ans.getOrDefault(k, 0.0), v));
                        }
                );
            }
            //
            leafInvertedIndexMap.put(curNode, leafIF);
            // Returns each string and the maximum weight of the current node.
            return ans;
        }
        if (curNode instanceof NonLeafDefault) {
            NonLeafDefault<String, Geometry> nonLeaf = (NonLeafDefault<String, Geometry>) curNode;
            Map<String, List<NodePair>> nodeIf = new HashMap<>(nonLeaf.count());
            Map<String, Double> ans = new HashMap<>(nonLeaf.count());
            for (Object child : nonLeaf.children()) {
                Node<String, Geometry> childNode = (Node<String, Geometry>) child;
                Map<String, Double> stringDoubleMap = buildIRtree(childNode);
                stringDoubleMap.forEach(
                        (k, v) -> {
                            nodeIf.putIfAbsent(k, new ArrayList<>());
                            nodeIf.get(k).add(NodePair.create(childNode, v));
                            ans.put(k, Double.max(ans.getOrDefault(k, 0.0), v));
                        }
                );
            }
            nodeInvertedIndexMap.put(curNode, nodeIf);
            return ans;
        }
        throw new IllegalArgumentException("Unsupported node type!");
    }

    @Setter
    @Getter
    public static class StringPair implements Comparable{

        private String key;

        private Double value;

        private StringPair(String key, Double value) {
            this.key = key;
            this.value = value;
        }

        StringPair() {
        }

        public static StringPair create(String key, Double value) {
            return new StringPair(key, value);
        }

        @Override
        public String toString() {
            return "StringPair{" +
                    "key='" + key + '\'' +
                    ", value=" + value +
                    '}';
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(value, ((StringPair)o).value);
        }
    }

    @Setter
    @Getter
    public static class NodePair implements Comparable {

        private Node<String, Geometry> key;

        private Double value;

        private NodePair(Node<String, Geometry> key, Double value) {
            this.key = key;
            this.value = value;
        }

        public static NodePair create(Node<String, Geometry> node, Double value) {
            return new NodePair(node, value);
        }

        @Override
        public String toString() {
            return "NodePair{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(value, ((NodePair) o).value);
        }
    }

    public Map<String, List<NodePair>> getNonLeafInvFile(Node<String, Geometry> node) {
        return nodeInvertedIndexMap.get(node);
    }

    public Map<String, List<StringPair>> getLeafInvFile(Node<String, Geometry> node) {
        return leafInvertedIndexMap.get(node);
    }

    public static void main(String[] args) {

        // x1 = -120.09514
        // y1 = 27.555126
        // x2 = -73.200455
        // y2 = 53.679195

    }

}