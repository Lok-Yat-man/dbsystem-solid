package std;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.Leaf;
import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.NonLeaf;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.HasGeometry;
import com.github.davidmoten.rtree.geometry.Rectangle;

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
import util.CommonAlgorithm;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.min;
import static java.lang.Double.max;

@Getter
public class BSTD {

    private IRTree irTree;

    private IRelevantObjectService relevantObjectService;

    private InvertedIndex<RelevantObject> invertedIndex;

    private final static Double smoothingFactor = 0.005;

    public BSTD() {

        relevantObjectService = new DefaultRelevantObjectServiceImpl();

        invertedIndex = new DefaultLeafInvertedIndex(relevantObjectService);

        irTree = new IRTree(relevantObjectService);

    }

    public BSTD(IRTree irTree, IRelevantObjectService relevantObjectService, InvertedIndex<RelevantObject> invertedIndex) {
        this.irTree = irTree;
        this.relevantObjectService = relevantObjectService;
        this.invertedIndex = invertedIndex;
    }

    public List<RelevantObject> bstd(Query query) {
        // S=∅; B=U
        List<Entry<String, Geometry>> S = new LinkedList<>();
        Optional<? extends Node<String, Geometry>> rootOptional = irTree.getRTree().root();
        if (!rootOptional.isPresent()) {
            throw new RuntimeException("RTree not exists!");
        }
        Node<String, Geometry> rootNode = rootOptional.get();
        double lon = query.getLocation().getLongitude();
        double lat = query.getLocation().getLatitude();
        Rectangle B = Geometries.rectangle(lon - 0.5, lat - 0.05, lon + 0.5, lat + 0.05);

        // MinHeap H=∅
        // Add root of IRTree to H, ∑(qi∈Q)〖st(qi,p)〗
        PriorityQueue<HasGeometry> minHeap = new PriorityQueue<>((o1, o2) -> {
            double stSum1 = 0.0, stSum2 = 0.0;
            stSum1 += st(o1, query);
            stSum2 += st(o2, query);
            return Double.compare(stSum1, stSum2);
        });
        minHeap.add(rootNode);

        while (!minHeap.isEmpty()) {
            HasGeometry e = minHeap.poll();
            Rectangle MBRe = generateUncertaintyMBR(e, query);
            if (MBRe.intersects(B)) {
                if (e instanceof Entry<?, ?>) {
                    if (S.isEmpty()) {
                        if (keywordsHave(e, query)) {
                            S.add((Entry<String, Geometry>) e);
                            B = getIntersectMBR(B, MBRe);
                        }
                    } else {
                        boolean isSkyline = true;
                        for (Entry<String, Geometry> p : S) {
                            Rectangle MBRp = generateUncertaintyMBR(p, query);
                            if (!MBRp.intersects(e.geometry().mbr()) || !keywordsHave(e, query)) {
                                isSkyline = false;
                                break;
                            }
                        }
                        if (isSkyline) {
                            S.add((Entry<String, Geometry>) e);
                            B = getIntersectMBR(B, MBRe);
                        }
                    }
                } else {
                    Node<String, Geometry> N = (Node<String, Geometry>) e;
                    if (N instanceof NonLeaf<String, Geometry>) {
                        for (Node e1 : ((NonLeaf<String, Geometry>) N).children()) {
                            Rectangle MBRe1 = generateUncertaintyMBR(e1, query);
                            if (MBRe1.intersects(B) && keywordsHave(e, query)) {
                                minHeap.add(e1);
                            }
                        }
                    } else if (N instanceof Leaf<String, Geometry>) {
                        for (Entry e1 : ((Leaf<String, Geometry>) N).entries()) {
                            Rectangle MBRe1 = generateUncertaintyMBR(e1, query);
                            if (MBRe1.intersects(B) && keywordsHave(e, query)) {
                                minHeap.add(e1);
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < S.size(); i++) {
            Entry<String, Geometry> entry = S.get(i);
            if (!entry.geometry().mbr().intersects(B)) {

//                System.out.println(S.get(i));

                S.remove(i);
                i--;
            }
        }

        System.out.println(B);

        List<RelevantObject> relevantObjects = S.stream()
                .map(Entry::value)
                .flatMap(value -> relevantObjectService.getByIds(List.of(value)).stream())
                .collect(Collectors.toList());

        return relevantObjects;
    }


    public Rectangle getIntersectMBR(Rectangle r1, Rectangle r2) {
        return Geometries.rectangle(max(r1.x1(), r2.x1()), max(r1.y1(), r2.y1()),
                min(r1.x2(), r2.x2()), min(r1.y2(), r2.y2()));
    }

    public Rectangle generateUncertaintyMBR(HasGeometry e, Query query) {
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

        Coordinate llCoordinate = CommonAlgorithm.getLLCoordinate(query.getLocation(), radius);
        Coordinate ruCoordinate = CommonAlgorithm.getRUCoordinate(query.getLocation(), radius);

        Rectangle MBR = Geometries.rectangleGeographic(
                llCoordinate.getLongitude(), llCoordinate.getLatitude(),
                ruCoordinate.getLongitude(), ruCoordinate.getLatitude());

        return MBR;
    }

    public boolean keywordsHave(HasGeometry e, Query query) {
        if (e instanceof NonLeafDefault<?, ?>) {
            Map<String, List<IRTree.NodePair>> nonLeafInvFile = irTree.getNonLeafInvFile((Node<String, Geometry>) e);
            boolean anyContain = query.getKeywords().stream().anyMatch(nonLeafInvFile::containsKey);
            return anyContain;
        } else if (e instanceof LeafDefault<?, ?>) {
            Map<String, List<IRTree.StringPair>> leafInvFile = irTree.getLeafInvFile((Node<String, Geometry>) e);
            boolean anyContain = query.getKeywords().stream().anyMatch(leafInvFile::containsKey);
            return anyContain;
        } else if (e instanceof Entry<?, ?>) {
            RelevantObject relevantObject = relevantObjectService.getById(((Entry<String, Geometry>) e).value());
            if (relevantObject == null) {
                return false;
            }
            List<String> weightKey = relevantObject.getLabels();
            boolean anyContain = query.getKeywords().stream().anyMatch(weightKey::contains);
            return anyContain;
        }
        return true;
    }


    public double st(HasGeometry e, Query query) {
        double logQ = query.getLocation().getLongitude();
        double latQ = query.getLocation().getLatitude();

        double logE = e.geometry().mbr().x1();
        double latE = e.geometry().mbr().y1();

        Coordinate coordinateQ = Coordinate.create(logQ, latQ);
        Coordinate coordinateE = Coordinate.create(logE, latE);
        double dist = CommonAlgorithm.calculateDistance(coordinateE, coordinateQ);
//        double dist = Math.sqrt(GeometryUtil.distanceSquared(logQ, latQ, logE, latE));
        if (e instanceof NonLeafDefault) {
            double w = w(query, (NonLeafDefault<String, Geometry>) e);
            if (Double.compare(w, 0) == 0)
                return Double.MAX_VALUE;
            return dist / w / 10;
        } else if (e instanceof LeafDefault) {
            double w = w(query, (LeafDefault<String, Geometry>) e);
            if (Double.compare(w, 0) == 0)
                return Double.MAX_VALUE;
            return dist / w / 10;
        } else if (e instanceof EntryDefault) {
            double w = w(query, (EntryDefault<String, Geometry>) e);
            if (Double.compare(w, 0) == 0)
                return Double.MAX_VALUE;
            return dist / w / 10;
        }
        return dist / 10;
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
        RelevantObject relevantObject = relevantObjectService.getById(relevantObjectId);
        if (relevantObject == null) {
            return 0;
        }
        List<String> weightKey = relevantObject.getLabels();
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
