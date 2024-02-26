package std;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.HasGeometry;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.GeometryUtil;
import com.github.davidmoten.rtree.internal.EntryDefault;
import com.github.davidmoten.rtree.internal.LeafDefault;
import com.github.davidmoten.rtree.internal.NonLeafDefault;
import entity.Coordinate;
import entity.Query;
import entity.RelevantObject;
import irtree.IRTree;
import ivtidx.DefaultLeafInvertedIndex;
import ivtidx.InvertedIndex;
import lombok.Getter;
import service.DefaultRelevantObjectServiceImpl;
import service.IRelevantObjectService;

import java.util.*;

import static java.lang.Double.min;
import static java.lang.Double.max;

@Getter
public class BSTD {

    private IRTree irTree;

    private InvertedIndex<RelevantObject> invertedIndex;

    private final static Double smoothingFactor = 0.02;

    public BSTD() {

        IRelevantObjectService relevantObjectService = new DefaultRelevantObjectServiceImpl();

        invertedIndex = new DefaultLeafInvertedIndex(relevantObjectService);

        irTree = new IRTree(relevantObjectService);

    }

    public BSTD(IRTree irTree, InvertedIndex<RelevantObject> invertedIndex) {
        this.irTree = irTree;
        this.invertedIndex = invertedIndex;
    }

    public List<Entry<String, Geometry>> bstd(List<Query> queries) {
        // S=∅; B=U
        List<Entry<String, Geometry>> S = new LinkedList<>();
        Optional<? extends Node<String, Geometry>> rootOptional = irTree.getRTree().root();
        if (!rootOptional.isPresent()) {
            throw new RuntimeException("RTree not exists!");
        }
        Node<String, Geometry> rootNode = rootOptional.get();
        Rectangle B = Geometries.rectangle(-180d, 0d, 179d, 90d);

        // MinHeap H=∅
        // Add root of IRTree to H, ∑(qi∈Q)〖st(qi,p)〗
        PriorityQueue<Node<String, Geometry>> minHeap = new PriorityQueue<>((o1, o2) -> {
            double stSum1 = 0.0, stSum2 = 0.0;
            for (Query query : queries) {
                stSum1 += st(o1, query);
                stSum2 += st(o2, query);
            }
            return Double.compare(stSum1, stSum2);
        });
        minHeap.add(rootNode);

        int countEmpty = 0, countCore = 0, countNon = 0;

        while (!minHeap.isEmpty()) {
            Node<String, Geometry> e = minHeap.poll();

            if (e.geometry().mbr().intersects(B)) {
                // e is a leaf node
                if (e instanceof LeafDefault) {

                    //System.out.println(e.getClass().getName());

                    if (S.isEmpty()) {
                        // Add e to S
                        // B = B ∩ Ru(e)
                        List<Entry<String, Geometry>> entries = ((LeafDefault<String, Geometry>) e).entries();
                        for (Query query : queries) {
                            for (Entry<String, Geometry> entry : entries) {
                                List<String> weightKey = invertedIndex.getValue(entry.value()).getWeightKey();
                                if (!Collections.disjoint(weightKey, query.getKeywords()))
                                    S.add(entry);
                            }
                        }
                        if (S.isEmpty()) {
                            continue;
                        }
                        Rectangle Rue = generateSkylinesMBR(S, queries);
                        B = getIntersectMBR(B, Rue);

                        //System.out.println("countEmpty" + (++countEmpty));

                    } else {
                        // for each p ∈ S
                        //对所有的skyline，先构造一个整体的不确定区域交集
                        Rectangle uncertainty = generateSkylinesMBR(S, queries);
                        // for each entry ∈ LeafNode
                        for (Entry<String, Geometry> ee : ((LeafDefault<String, Geometry>) e).entries()) {
                            if (isDominant(ee, uncertainty)) {
                                continue;
                            }
                            S.add(ee);
                            Rectangle Rue = generateUncertaintyMBR(ee, queries);
                            B = getIntersectMBR(B, Rue);
                            uncertainty = generateSkylinesMBR(S, queries);

                            //System.out.println(B);
                        }

                        System.out.println("countCore" + (++countCore));

                    }
                }
                // e is a non-leaf node
                else if (e instanceof NonLeafDefault) {

                    for (Node<String, Geometry> ee : ((NonLeafDefault<String, Geometry>) e).children()) {
                        if (ee.geometry().mbr().intersects(B)) {
                            minHeap.add(ee);
                        }
                    }
                    //System.out.println("countNon" + (++countNon));
                }
            }
        }
        System.out.println(B);

        return S;
    }

    public Rectangle getIntersectMBR(Rectangle r1, Rectangle r2) {
        return Geometries.rectangle(max(r1.x1(), r2.x1()), max(r1.y1(), r2.y1()),
                min(r1.x2(), r2.x2()), min(r1.y2(), r2.y2()));
    }

    public Rectangle generateUncertaintyMBR(HasGeometry e, List<Query> queries) {
        double x = e.geometry().mbr().x1();
        double y = e.geometry().mbr().y1();
        Rectangle MBR = Geometries.rectangle(x, y, x, y);

        for (Query query : queries) {
            double qx = query.getLocation().getLongitude();
            double qy = query.getLocation().getLatitude();
            double radius = 0;
            if (e instanceof NonLeafDefault) {
                double st = st((NonLeafDefault<String, Geometry>) e, query);
                if (Double.compare(st, Double.MAX_VALUE) == 0)
                    return Geometries.rectangle(-180d, 0d, 179d, 90d);
                radius = st;
            } else if (e instanceof LeafDefault) {
                double st = st((LeafDefault<String, Geometry>) e, query);
                if (Double.compare(st, Double.MAX_VALUE) == 0)
                    return Geometries.rectangle(-180d, 0d, 179d, 90d);
                radius = st;
            } else if (e instanceof EntryDefault) {
                double st = st((EntryDefault<String, Geometry>) e, query);
                if (Double.compare(st, Double.MAX_VALUE) == 0)
                    return Geometries.rectangle(-180d, 0d, 179d, 90d);
                radius = st;
            }
            MBR = MBR.add(Geometries.rectangle(qx - radius, qy - radius, qx + radius, qy + radius));
        }
        return MBR;
    }

    public Rectangle generateSkylinesMBR(List<Entry<String, Geometry>> S, List<Query> queries) {
        Rectangle MBR = Geometries.rectangle(-180d, 0d, 179d, 90d);
        for (Entry<String, Geometry> s : S) {
            Rectangle Rus = generateUncertaintyMBR(s, queries);
            MBR = getIntersectMBR(MBR, Rus);
        }
        return MBR;
    }

    public boolean isDominant(Entry<String, Geometry> e, Rectangle uncertainty) {
        RelevantObject relevantObject = invertedIndex.getValue(e.value());
        double log = relevantObject.getCoordinate().getLongitude();
        double lat = relevantObject.getCoordinate().getLatitude();
        return !uncertainty.contains(log, lat);
    }


    public double st(HasGeometry e, Query query) {
        double logQ = query.getLocation().getLongitude();
        double latQ = query.getLocation().getLatitude();

        double logE = e.geometry().mbr().x1();
        double latE = e.geometry().mbr().y1();

        //double dist = CommonAlgorithm.calculateDistance(coordinateE, coordinateQ);
        double dist = Math.sqrt(GeometryUtil.distanceSquared(logQ, latQ, logE, latE));
        if (e instanceof NonLeafDefault) {
            double w = w(query, (NonLeafDefault<String, Geometry>) e);
            if (Double.compare(w, 0) == 0)
                return Double.MAX_VALUE;
            return dist / w;
        } else if (e instanceof LeafDefault) {
            double w = w(query, (LeafDefault<String, Geometry>) e);
            if (Double.compare(w, 0) == 0)
                return Double.MAX_VALUE;
            return dist / w;
        } else if (e instanceof EntryDefault) {
            double w = w(query, (EntryDefault<String, Geometry>) e);
            if (Double.compare(w, 0) == 0)
                return Double.MAX_VALUE;
            return dist / w;
        }
        return dist;
    }

    public double w(Query query, NonLeafDefault<String, Geometry> node) {
        Map<String, List<IRTree.NodePair>> nonLeafInvFile = irTree.getNonLeafInvFile(node);
        boolean anyContain = query.getKeywords().stream().anyMatch(nonLeafInvFile::containsKey);
        if (!anyContain) {
            return 0;
        }
        double weight = 1.0;
        for (String queryString : query.getKeywords()) {
            if (nonLeafInvFile.containsKey(queryString)) {
                List<IRTree.NodePair> nodePairs = nonLeafInvFile.get(queryString);
                Optional<IRTree.NodePair> maxNodePair = nodePairs.stream().max(IRTree.NodePair::compareTo);
                if (maxNodePair.isPresent()) {
                    weight *= maxNodePair.get().getValue();
                }
            } else {
                weight *= smoothingFactor;
            }
        }
        weight = Math.pow(weight, 1.0 / query.getKeywords().size());
        return weight;
    }

    public double w(Query query, LeafDefault<String, Geometry> node) {
        Map<String, List<IRTree.StringPair>> leafInvFile = irTree.getLeafInvFile(node);
        boolean anyContain = query.getKeywords().stream().anyMatch(leafInvFile::containsKey);
        if (!anyContain) {
            return 0;
        }
        double weight = 1.0;
        for (String queryString : query.getKeywords()) {
            if (leafInvFile.containsKey(queryString)) {
                List<IRTree.StringPair> nodePairs = leafInvFile.get(queryString);
                Optional<IRTree.StringPair> maxStringPair = nodePairs.stream().max(IRTree.StringPair::compareTo);
                if (maxStringPair.isPresent()) {
                    weight *= maxStringPair.get().getValue();
                }
            } else {
                weight *= smoothingFactor;
            }
        }
        weight = Math.pow(weight, 1.0 / query.getKeywords().size());
        return weight;
    }

    public double w(Query query, EntryDefault<String, Geometry> entry) {
        String relevantObjectId = entry.value();
        RelevantObject relevantObject = invertedIndex.getValue(relevantObjectId);
        List<String> weightKey = relevantObject.getWeightKey();
        boolean anyContain = query.getKeywords().stream().anyMatch(weightKey::contains);
        if (!anyContain) {
            return 0;
        }
        double weight = 1.0;
        for (String queryString : query.getKeywords()) {
            if (weightKey.contains(queryString)) {
                weight *= relevantObject.getWeights().get(queryString);
            } else {
                weight *= smoothingFactor;
            }
        }
        weight = Math.pow(weight, 1.0 / query.getKeywords().size());
        return weight;
    }
}
