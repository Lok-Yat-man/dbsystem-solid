package cn.edu.szu.cs;

import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.internal.LeafDefault;
import com.github.davidmoten.rtree.internal.NonLeafDefault;


import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  IRtree
 * @author Whitence
 * @date 2023/10/8 16:39
 * @version 1.0
 */
public final class SimpleIRTree implements IRTree {


    /**
     * rTree
     */
    private RTree<String, Geometry> rTree;
    /**
     * Record the relationship between non-leaf nodes and IF
     */
    private Map<Node<String, Geometry>,Map<String,List<NodePair>>> nodeInvertedIndexMap;
    /**
     * Record the relationship between leaf nodes and IF
     */
    private Map<Node<String, Geometry>,Map<String,List<StringPair>>> leafInvertedIndexMap;

    private IRelevantObjectService relevantObjectService;


    public SimpleIRTree(IRelevantObjectService relevantObjectService){

        try(
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("rtree.txt");
        ){
            // deserialize rTree
            RTree<String, Geometry> rTree = Serializers.flatBuffers().utf8().read(
                    inputStream,
                    inputStream.available(),
                    InternalStructure.DEFAULT
            );

            assert relevantObjectService != null && rTree !=null && rTree.root().isPresent();

            this.rTree = rTree;
            this.relevantObjectService = relevantObjectService;

            nodeInvertedIndexMap = new HashMap<>();
            leafInvertedIndexMap = new HashMap<>();

            buildIRtree(rTree.root().get());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public SimpleIRTree(RTree<String, Geometry> rTree,IRelevantObjectService relevantObjectService) {
        assert relevantObjectService != null;
        assert rTree !=null && rTree.root().isPresent();

        this.rTree = rTree;
        this.relevantObjectService = relevantObjectService;

        nodeInvertedIndexMap = new HashMap<>();
        leafInvertedIndexMap = new HashMap<>();

        buildIRtree(rTree.root().get());

    }

    private Map<String,Double> buildIRtree(Node<String,Geometry> curNode){

        // Processing leaf nodes
        if(curNode instanceof LeafDefault){

            LeafDefault leaf = (LeafDefault) curNode;

            List<Entry<String, Point>> entries = leaf.entries();

            Map<String,List<StringPair>> leafIF = new HashMap<>(leaf.count());
            Map<String,Double> ans = new HashMap<>(leaf.count());

            for (Entry<String, Point> entry : entries) {

                String id = entry.value();

                relevantObjectService.getWeightsById(id).forEach(
                        (k,v)->{
                            // 计算当前节点的IF
                            leafIF.putIfAbsent(k,new ArrayList<>());
                            leafIF.get(k).add(StringPair.create(id,v));

                            // 计算当前节点每个字符串的最大权重
                            ans.put(k,Double.max(ans.getOrDefault(k,0.0),v));

                        }
                );

            }
            //
            leafInvertedIndexMap.put(curNode,leafIF);

            // Returns each string and the maximum weight of the current node.
            return ans;

        }

        if(curNode instanceof NonLeafDefault){

            NonLeafDefault<String,Geometry> nonLeaf = (NonLeafDefault<String,Geometry>) curNode;

            Map<String,List<NodePair>> nodeIf = new HashMap<>(nonLeaf.count());

            Map<String,Double> ans = new HashMap<>(nonLeaf.count());

            for (Object child : nonLeaf.children()) {

                Node<String, Geometry> childNode = (Node<String, Geometry>) child;

                Map<String, Double> stringDoubleMap = buildIRtree(childNode);

                stringDoubleMap.forEach(
                        (k,v)->{
                            nodeIf.putIfAbsent(k,new ArrayList<>());
                            nodeIf.get(k).add(NodePair.create(childNode,v));

                            ans.put(k,Double.max(ans.getOrDefault(k,0.0),v));
                        }
                );
            }
            //
            nodeInvertedIndexMap.put(curNode,nodeIf);

            return ans;
        }

        throw new IllegalArgumentException("Unsupported node type!");
    }


    private static class StringPair{

        private String key;

        private Double value;

        private StringPair(String key,Double value){
            this.key=key;
            this.value=value;
        }
        StringPair(){

        }

        public static StringPair create(String key,Double value){
            return new StringPair(key,value);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

    }

    private static class NodePair{

        private Node<String, Geometry> key;

        private Double value;

        private NodePair(Node<String, Geometry> key, Double value) {
            this.key = key;
            this.value = value;
        }

        public static NodePair create(Node<String,Geometry> node,Double value){
            return new NodePair(node,value);
        }

        public Node<String, Geometry> getKey() {
            return key;
        }

        public void setKey(Node<String, Geometry> key) {
            this.key = key;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    @Override
    public synchronized List<RelevantObject> rangeQuery(Query query, RelevantObject p) {

        Optional<? extends Node<String, Geometry>> optional = rTree.root();

        if(!optional.isPresent()){
            throw new RuntimeException("RTree not exists!");
        }

        Node<String, Geometry> node = optional.get();

        GeoPointDouble geoPointDouble = GeoPointDouble.create(
                p.getCoordinate().getLongitude(),
                p.getCoordinate().getLatitude()
        );

        List<RelevantObject> list = new LinkedList<>();

        Deque<Node<String, Geometry>> dq = new LinkedList<>();

        dq.add(node);

        while(!dq.isEmpty()){

            Node<String, Geometry> curNode = dq.poll();

            if(curNode instanceof LeafDefault){

                Map<String, List<StringPair>> leafIf = leafInvertedIndexMap.get(curNode);

                List<String> objIds = Optional.ofNullable(
                        query.getKeywords()
                ).map(
                        kwds -> kwds.stream()
                                .map(leafIf::get)
                                .filter(Objects::nonNull)
                                .reduce(new ArrayList<>(), (a, b) -> {
                                    a.addAll(b);
                                    return a;
                                })
                                .stream()
                                .map(StringPair::getKey)
                                .collect(Collectors.toList())
                ).orElse(new ArrayList<>());


                List<RelevantObject> relevantObjects = relevantObjectService.getByIds(objIds);

                relevantObjects = relevantObjects.stream()
                                .filter(
                                        relevantObject -> geoPointDouble.distance(
                                        GeoPointDouble.create(
                                                relevantObject.getCoordinate().getLongitude(),
                                                relevantObject.getCoordinate().getLatitude()
                                        )
                                ) <= query.getEpsilon())
                                        .collect(Collectors.toList());


                list.addAll(relevantObjects);

            }

            if(curNode instanceof NonLeafDefault){

                Map<String, List<NodePair>> nodeIf = nodeInvertedIndexMap.get(curNode);

                dq.addAll(
                        Optional.ofNullable(
                                query.getKeywords()
                        ).map(
                                kwds -> kwds.stream()
                                        .map(nodeIf::get)
                                        .filter(Objects::nonNull)
                                        .reduce(new ArrayList<>(), (a, b) -> {
                                            a.addAll(b);
                                            return a;
                                        })
                                        .stream()
                                        .map(NodePair::getKey)
                                        .collect(Collectors.toList())
                        ).orElse(new ArrayList<>())
                );

            }

        }
        return list;
    }

}
