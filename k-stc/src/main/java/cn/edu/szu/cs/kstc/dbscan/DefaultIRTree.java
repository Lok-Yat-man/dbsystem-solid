package cn.edu.szu.cs.kstc.dbscan;

import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.GeoPointDouble;
import cn.edu.szu.cs.util.CommonUtil;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.internal.LeafDefault;
import com.github.davidmoten.rtree.internal.NonLeafDefault;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * IRtree
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/10/8 16:39
 */
public final class DefaultIRTree implements DbScanBasedApproach.IRTree<DbScanRelevantObject> {

    /**
     * rTree
     */
    private final RTree<DbScanRelevantObject, GeoPointDouble> rTree;
    /**
     * Record the relationship between non-leaf nodes and IF
     */
    private final Map<Node<DbScanRelevantObject, GeoPointDouble>, Map<String, List<IRtreeNode>>> nodeInvertedIndexMap;

    /**
     * Record the relationship between leaf nodes and IF
     */
    private final Map<Node<DbScanRelevantObject, GeoPointDouble>, Map<String, List<IRtreeLeafNode>>> leafNodeInvertedIndexMap;

    /**
     * Cache
     */
    private static final LFUCache<String, List<Entry<DbScanRelevantObject, GeoPointDouble>>> cache = CacheUtil.newLFUCache(100);

    @Data
    private static class IRtreeNode {
        private Node<DbScanRelevantObject, GeoPointDouble> node;
        private Double weight;

        public IRtreeNode(Node<DbScanRelevantObject, GeoPointDouble> node, Double weight) {
            this.node = node;
            this.weight = weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IRtreeNode that = (IRtreeNode) o;
            return Objects.equals(node, that.node) && Objects.equals(weight, that.weight);
        }

        @Override
        public int hashCode() {
            return Objects.hash(node, weight);
        }
    }
    @Data
    private static class IRtreeLeafNode {
        private Entry<DbScanRelevantObject, GeoPointDouble> entry;
        private Double weight;

        public IRtreeLeafNode(Entry<DbScanRelevantObject, GeoPointDouble> entry, Double weight) {
            this.entry = entry;
            this.weight = weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IRtreeLeafNode that = (IRtreeLeafNode) o;
            return Objects.equals(entry, that.entry) && Objects.equals(weight, that.weight);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entry, weight);
        }
    }




    /**
     * Build IRtree
     *
     * @param rTree rTree
     */
    public DefaultIRTree(RTree<DbScanRelevantObject, GeoPointDouble> rTree) {
        assert rTree != null && rTree.root().isPresent();
        this.rTree = rTree;

        nodeInvertedIndexMap = new HashMap<>();
        leafNodeInvertedIndexMap = new HashMap<>();

        buildIRtree(rTree.root().get());
    }

    @SuppressWarnings("all")
    private Map<String, Double> buildIRtree(Node<DbScanRelevantObject, GeoPointDouble> curNode) {

        // Processing leaf nodes
        if (curNode instanceof LeafDefault) {
            LeafDefault<DbScanRelevantObject, GeoPointDouble> leaf = (LeafDefault) curNode;

            Map<String, List<IRtreeLeafNode>> iTreeLeafNodeMap = new HashMap<>();

            List<Entry<DbScanRelevantObject, GeoPointDouble>> entries = leaf.entries();

            Map<String, Double> ans = new HashMap<>();
            for (Entry<DbScanRelevantObject, GeoPointDouble> entry : entries) {

                DbScanRelevantObject relatedObject = entry.value();

                List<String> labels = relatedObject.getLabels();

                for (String label : labels) {
                    iTreeLeafNodeMap.putIfAbsent(label, new ArrayList<>());
                    iTreeLeafNodeMap.get(label).add(
                            new IRtreeLeafNode(entry, relatedObject.getWeight(label))
                    );
                    Double weight = ans.getOrDefault(label, 0.0);
                    if (weight < relatedObject.getWeight(label)) {
                        ans.put(label, relatedObject.getWeight(label));
                    }
                }

            }

            leafNodeInvertedIndexMap.put(curNode, iTreeLeafNodeMap);

            return ans;
        }

        if (curNode instanceof NonLeafDefault) {
            NonLeafDefault<DbScanRelevantObject, GeoPointDouble> nonLeaf = (NonLeafDefault<DbScanRelevantObject, GeoPointDouble>) curNode;

            Map<String, List<IRtreeNode>> nodeIf = new HashMap<>(nonLeaf.count());

            Map<String, Double> ans = new HashMap<>();
            for (Object child : nonLeaf.children()) {
                Node<DbScanRelevantObject, GeoPointDouble> childNode = (Node<DbScanRelevantObject, GeoPointDouble>) child;

                Map<String, Double> labelWeightMap = buildIRtree(childNode);

                for (Map.Entry<String, Double> entry : labelWeightMap.entrySet()) {
                    String label = entry.getKey();
                    Double weight = entry.getValue();
                    nodeIf.putIfAbsent(label, new ArrayList<>());
                    nodeIf.get(label).add(
                            new IRtreeNode(childNode, weight)
                    );
                    Double curWeight = ans.getOrDefault(label, 0.0);
                    if (curWeight < weight) {
                        ans.put(label, weight);
                    }
                }

            }
            //
            nodeInvertedIndexMap.put(curNode, nodeIf);
            return ans;
        }

        throw new IllegalArgumentException("Unsupported node type!");
    }

    private List<IRtreeNode> getNodesByKeyword(Node<DbScanRelevantObject, GeoPointDouble> curNode, String keyword) {
        Map<String, List<IRtreeNode>> stringListMap = nodeInvertedIndexMap.get(curNode);
        return stringListMap.getOrDefault(keyword, new ArrayList<>());
    }

    private Set<IRtreeNode> getNodesByKeywords(Node<DbScanRelevantObject,GeoPointDouble> curNode, List<String> keywords) {
        Set<IRtreeNode> nodes = new HashSet<>(getNodesByKeyword(curNode, keywords.get(0)));

        for (int i = 1; i < keywords.size(); i++) {
            nodes.retainAll(getNodesByKeyword(curNode, keywords.get(i)));
        }

        return nodes;
    }

    private List<Entry<DbScanRelevantObject, GeoPointDouble>> filterByKeywords(List<String> keywords) {
        if (CollUtil.isEmpty(keywords)) {
            return new ArrayList<>();
        }
        if (cache.containsKey(keywords.toString())) {
            return cache.get(keywords.toString());
        }

        Node<DbScanRelevantObject, GeoPointDouble> root = rTree.root().get();

        Deque<Node<DbScanRelevantObject, GeoPointDouble>> que = new ArrayDeque<>();
        que.add(root);
        while (!que.isEmpty()) {
            Node<DbScanRelevantObject, GeoPointDouble> node = que.peek();
            if (node instanceof LeafDefault) {
                break;
            }
            //
            if (node instanceof NonLeafDefault) {
                int size = que.size();
                for (int i = 0; i < size; i++) {
                    Node<DbScanRelevantObject, GeoPointDouble> curNode = que.poll();
                    Set<IRtreeNode> nodesByKeywords = getNodesByKeywords(curNode, keywords);
                    que.addAll(
                            nodesByKeywords.stream()
                                    .map(IRtreeNode::getNode)
                                    .collect(Collectors.toList())
                    );
                }
            }
        }

        List<Entry<DbScanRelevantObject, GeoPointDouble>> entries = que.stream()
                .map(node -> ((LeafDefault<DbScanRelevantObject, GeoPointDouble>) node).entries())
                .reduce(new ArrayList<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                });

        cache.put(keywords.toString(), entries);
        return entries;

    }

    @Override
    public synchronized Queue<DbScanRelevantObject> rangeQuery(List<String> keywords, double[] coordinate, double epsilon) {
        Assert.isTrue(rTree.root().isPresent(), "rtree not exist.");
        Assert.isTrue(epsilon > 0.0, "rtree not exist.");
        Assert.checkBetween(coordinate[0], -180.0, 180.0, "wrong lon.");
        Assert.checkBetween(coordinate[1], -90, 90, "wrong lat.");
        if (keywords == null || keywords.isEmpty()) {
            return new LinkedList<>();
        }

        List<Entry<DbScanRelevantObject, GeoPointDouble>> entries = filterByKeywords(keywords);

        return entries.stream()
                .filter(entry -> entry.geometry().distance(GeoPointDouble.create(coordinate[0], coordinate[1])) < epsilon)
                .map(Entry::value)
                .collect(Collectors.toCollection(LinkedList::new));

    }

    @Override
    public Double getMaxDistance() {
        Assert.isTrue(rTree.root().isPresent(), "rtree not exist.");
        Node<DbScanRelevantObject, GeoPointDouble> node = rTree.root().get();
        return CommonUtil.getDistance(node.geometry().mbr().x1(), node.geometry().mbr().y1(),
                node.geometry().mbr().x2(), node.geometry().mbr().y2());
    }
}
