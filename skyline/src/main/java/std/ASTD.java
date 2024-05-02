package std;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.HasGeometry;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleDouble;
import com.github.davidmoten.rtree.internal.EntryDefault;
import com.github.davidmoten.rtree.internal.LeafDefault;
import com.github.davidmoten.rtree.internal.NonLeafDefault;
import entity.Coordinate;
import entity.Query;
import entity.RelevantObject;
import irtree.IRTree;
import ivtidx.DefaultLeafInvertedIndex;
import ivtidx.InvertedIndex;
import rx.Observable;
import rx.Subscription;
import service.DefaultRelevantObjectServiceImpl;
import service.IRelevantObjectService;
import util.CommonAlgorithm;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.max;
import static java.lang.Double.min;

public class ASTD {
    private IRTree irTree;

    private IRelevantObjectService relevantObjectService;

    private InvertedIndex<RelevantObject> invertedIndex;

    private final static Double smoothingFactor = 0.005;

    public ASTD() {

        relevantObjectService = new DefaultRelevantObjectServiceImpl();

        invertedIndex = new DefaultLeafInvertedIndex(relevantObjectService);

        irTree = new IRTree(relevantObjectService);

    }

    public ASTD(IRTree irTree, IRelevantObjectService relevantObjectService, InvertedIndex<RelevantObject> invertedIndex) {
        this.irTree = irTree;
        this.relevantObjectService = relevantObjectService;
        this.invertedIndex = invertedIndex;
    }

    public List<RelevantObject> astd(Query query) {
        // S=∅; B=U
        List<Entry<String, Geometry>> S = new LinkedList<>();

        double lon = query.getLocation().getLongitude();
        double lat = query.getLocation().getLatitude();
        Rectangle B = Geometries.rectangle(lon - 0.5, lat - 0.05, lon + 0.5, lat + 0.05);

        // MinHeap H=∅

        PriorityQueue<HasGeometry> minHeap = new PriorityQueue<>((o1, o2) -> {
            double stSum1 = 0.0, stSum2 = 0.0;
            stSum1 += st(o1, query);
            stSum2 += st(o2, query);
            return Double.compare(stSum1, stSum2);
        });

        RTree<String, Rectangle> skyRtree = RTree.create();

        // Add root of IRTree to H, ∑(qi∈Q)〖st(qi,p)〗
        Optional<? extends Node<String, Geometry>> rootOptional = irTree.getRTree().root();
        if (rootOptional.isEmpty()) {
            throw new RuntimeException("IRTree not exists!");
        }
        Node<String, Geometry> rootNode = rootOptional.get();
        minHeap.add(rootNode);

        while (!minHeap.isEmpty()) {
            HasGeometry e = minHeap.poll();
            if (e.geometry().mbr().intersects(B)) {
                if (e instanceof Entry) {
                    if (keywordsHave(e, query) && (S.isEmpty() || !skyRTreePrunes(skyRtree, e, query))) {
                        S.add((Entry<String, Geometry>) e);
                        Rectangle Ru = generateUncertaintyMBR(e, query);
                        B = getIntersectMBR(B, Ru);
                        //skyRtree = skyRtree.add((Entry<String, Geometry>) e);
                        Entry<?, Rectangle> entry = EntryDefault.entry(((Entry<?, ?>) e).value(), Ru);
                        skyRtree = skyRtree.add((Entry<String, Rectangle>) entry);
                    }
                } else {
                    if (keywordsHave(e, query) && !skyRTreePrunes(skyRtree, e, query)) {
                        Node<String, Geometry> N = (Node<String, Geometry>) e;
                        if (N instanceof NonLeafDefault<String, Geometry>) {
                            for (Node<String, Geometry> e1 : ((NonLeafDefault<String, Geometry>) N).children()) {
                                if (e1.geometry().mbr().intersects(B)) {
                                    minHeap.add(e1);
                                }
                            }
                        } else if (N instanceof LeafDefault<String, Geometry>) {
                            for (Entry<String, Geometry> e1 : ((LeafDefault<String, Geometry>) N).entries()) {
                                if (e1.geometry().mbr().intersects(B)) {
                                    minHeap.add(e1);
                                }
                            }
                        }
                    }
                }
            }
        }

        List<RelevantObject> relevantObjects = S.stream()
                .map(Entry::value)
                .flatMap(value -> relevantObjectService.getByIds(List.of(value)).stream())
                .collect(Collectors.toList());

        return relevantObjects;
    }

    public boolean skyRTreePrunes(RTree<String, Rectangle> skyRtree, HasGeometry p, Query query) {
        Optional<? extends Node<String, Rectangle>> root = skyRtree.root();
        if (root.isEmpty()) {
            return false;
        }
        Node<String, Rectangle> rootNode = root.get();
        Rectangle upperCorner = rootNode.geometry().mbr();
        if (!upperCorner.intersects(p.geometry().mbr())) {
            return true;
        }
        Queue<Node<String, Rectangle>> nodes = new ArrayDeque<>();
        nodes.add(rootNode);
        while (!nodes.isEmpty()) {
            Node<String, Rectangle> N = nodes.remove();
            if (N instanceof NonLeafDefault<String, Rectangle>) {
                for (Node<String, Rectangle> childNode : ((NonLeafDefault<String, Rectangle>) N).children()) {
                    Rectangle upperCorner1 = childNode.geometry().mbr();
                    if (!upperCorner.intersects(p.geometry().mbr())) {
                        return true;
                    }
                    nodes.add(childNode);
                }
            } else {
                for (Entry<String, Rectangle> e : ((LeafDefault<String, Rectangle>) N).entries()) {
                    if (!e.geometry().mbr().intersects(p.geometry().mbr())) {
                        return true;
                    }
                }
            }
        }
        return false;
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
            List<String> weightKey = relevantObject.getLabels();
            boolean anyContain = query.getKeywords().stream().anyMatch(weightKey::contains);
            return anyContain;
        }
        return true;
    }

    public Rectangle generateUncertaintyMBR(HasGeometry e, Query query) {
        double x = e.geometry().mbr().x1();
        double y = e.geometry().mbr().y1();
        Rectangle MBR = Geometries.rectangle(x, y, x, y);

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
        try {
            MBR = MBR.add(Geometries.rectangle(
                    llCoordinate.getLongitude(), llCoordinate.getLatitude(),
                    ruCoordinate.getLongitude(), ruCoordinate.getLatitude()));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return MBR;
    }

    public Rectangle getIntersectMBR(Rectangle r1, Rectangle r2) {
        return Geometries.rectangle(max(r1.x1(), r2.x1()), max(r1.y1(), r2.y1()),
                min(r1.x2(), r2.x2()), min(r1.y2(), r2.y2()));
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
        RelevantObject relevantObject = invertedIndex.getValue(relevantObjectId);
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
