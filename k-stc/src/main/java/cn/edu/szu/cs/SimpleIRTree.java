package cn.edu.szu.cs;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.internal.LeafDefault;
import com.github.davidmoten.rtree.internal.NonLeafDefault;
import rx.Observable;
import rx.functions.Func2;


import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  IRtree
 * @author Whitence
 * @date 2023/10/8 16:39
 * @version 1.0
 */
public final class SimpleIRTree implements IRTree<RelatedObject> {

    /**
     * rTree
     */
    private RTree<String, Geometry> rTree;
    /**
     * Record the relationship between non-leaf nodes and IF
     */
    private Map<Node<String,Geometry>,Map<String,List<Node<String, Geometry>>>> nodeInvertedIndexMap;

    private LFUCache<String,List<Entry<String,Geometry>>> cache = CacheUtil.newLFUCache(100);

    private IRelatedObjectService relatedObjectService;

    public SimpleIRTree(RTree<String, Geometry> rTree,IRelatedObjectService relevantObjectService) {
        assert relevantObjectService != null;
        assert rTree !=null && rTree.root().isPresent();
        this.rTree = rTree;
        this.relatedObjectService = relevantObjectService;
        nodeInvertedIndexMap = new HashMap<>();
        buildIRtree(rTree.root().get());

    }

    @SuppressWarnings("unchecked")
    private Set<String> buildIRtree(Node<String,Geometry> curNode){

        // Processing leaf nodes
        if(curNode instanceof LeafDefault){
            LeafDefault leaf = (LeafDefault) curNode;
            List<Entry<String,Point>> entries = leaf.entries();
            Set<String> set = new HashSet<>();

            for (Entry<String, Point> entry : entries) {
                List<String> labels = relatedObjectService.getLabelsById(entry.value()).stream().map(String::toLowerCase).collect(Collectors.toList());
                set.addAll(labels);
            }

            // Returns each string and the maximum weight of the current node.
            return set;

        }

        if(curNode instanceof NonLeafDefault){

            NonLeafDefault<String,Geometry> nonLeaf = (NonLeafDefault<String,Geometry>) curNode;

            Map<String,List<Node<String, Geometry>>> nodeIf = new HashMap<>(nonLeaf.count());

            Set<String> ans = new HashSet<>();

            for (Object child : nonLeaf.children()) {
                Node<String, Geometry> childNode = (Node<String, Geometry>) child;
                Set<String>  stringSet = buildIRtree(childNode);
                ans.addAll(stringSet);
                for (String str : stringSet) {
                    nodeIf.putIfAbsent(str,new ArrayList<>());
                    nodeIf.get(str).add(childNode);
                }

            }
            //
            nodeInvertedIndexMap.put(curNode,nodeIf);

            return ans;
        }

        throw new IllegalArgumentException("Unsupported node type!");
    }

    private List<Node<String,Geometry>> getNodesByKeyword(Node<String, Geometry> curNode,String keyword){
        return Optional.ofNullable(
                nodeInvertedIndexMap.get(curNode).get(keyword)
        ).orElse(new ArrayList<>());
    }

    private List<Entry<String,Geometry>> filterByKeywords(List<String> keywords){

        if(CollUtil.isEmpty(keywords)){
            return new ArrayList<>();
        }

        if(cache.containsKey(keywords.toString())){
            return cache.get(keywords.toString());
        }


        Node<String, Geometry> root = rTree.root().get();

        Deque<Node<String,Geometry>> que = new ArrayDeque<>();

        que.add(root);

        while(!que.isEmpty()){

            Node<String, Geometry> node = que.peek();

            if(node instanceof LeafDefault){
                break;
            }
            //
            if(node instanceof NonLeafDefault){
                int size = que.size();
                for (int i = 0; i < size; i++) {

                    Node<String, Geometry> curNode = que.poll();

                    String keyword = keywords.get(0);

                    Set<Node<String,Geometry>> set = new HashSet<>(getNodesByKeyword(curNode,keyword));

                    for(int j=1;j<keywords.size();j++){
                        set.retainAll(getNodesByKeyword(curNode,keywords.get(j)));
                    }
                    que.addAll(set);
                }
            }
        }

        List<Entry<String, Geometry>> list = Optional.ofNullable(
                que.stream()
                        .map(node -> ((LeafDefault<String, Geometry>) node).entries())
                        .reduce(new ArrayList<>(), (a, b) -> {
                            a.addAll(b);
                            return a;
                        })
        ).orElse(new ArrayList<>());

        cache.put(keywords.toString(),list);

        return list;

    }

    @Override
    public synchronized List<RelatedObject> rangeQuery(List<String> keywords,Coordinate coordinate,double epsilon) {

        Assert.isTrue(rTree.root().isPresent(),"rtree not exist.");
        Assert.isTrue(epsilon>0.0,"rtree not exist.");
        Assert.checkBetween(coordinate.getLongitude(),-180.0,180.0,"wrong lon.");
        Assert.checkBetween(coordinate.getLatitude(),-90,90,"wrong lat.");

        if(keywords == null || keywords.isEmpty()){
            return Collections.emptyList();
        }

        return relatedObjectService.getByIds(
                filterByKeywords(keywords)
                        .stream()
                        .filter(
                                entry -> CommonAlgorithm.calculateDistance(
                                        Coordinate.create(entry.geometry().mbr().x1(), entry.geometry().mbr().y1()),
                                        coordinate
                                ) < epsilon
                        ).map(Entry::value)
                        .collect(Collectors.toList())
        ).stream().filter(
                relatedObject -> {

                    String str = String.join(" ", relatedObject.getLabels()).toLowerCase();

                    for (String keyword : keywords) {
                        if(!str.contains(keyword)){
                            return false;
                        }
                    }
                    return true;
                }
        ).collect(Collectors.toList());
        
    }




}
