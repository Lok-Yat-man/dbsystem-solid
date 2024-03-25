import com.github.davidmoten.rtree.geometry.Geometry;
import entity.RelevantObject;
import ivtidx.DefaultLeafInvertedIndex;
import ivtidx.InvertedIndex;
import std.BSTD;
import com.github.davidmoten.rtree.Entry;
import entity.Coordinate;
import entity.Query;
import org.junit.jupiter.api.Test;
import service.DefaultRelevantObjectServiceImpl;
import service.IRelevantObjectService;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BSTDTest {
    private BSTD b = new BSTD();
    @Test
    public void bstdTest(){
        LinkedList<Query> queries = new LinkedList<>();
        // 边界值：
        // x1 = -120.09514 y1 = 27.555126
        // x2 = -73.200455 y2 = 53.679195

/*
        Query query1 = Query.create(
                Coordinate.create(
                        -74.16256713867188,
                        39.94322204589844
                ),
                Arrays.asList("Restaurants")
                //Arrays.asList("Water")
        );
*/
        Query query2 = Query.create(
                Coordinate.create(
                        -75.1,
                        40.1
                ),
                Arrays.asList("Chinese", "Restaurant")
        );

        //queries.add(query1);
        queries.add(query2);


//        List<Entry<String, Geometry>> valuesEntry = b.bstd(queries);
        List<RelevantObject> relevantObjects = b.bstd(queries);

//        List<String> values = valuesEntry.stream()
//                .map(Entry::value)
//                .collect(Collectors.toList());
//
//
//        //System.out.println(valuesEntry);
//
//
        System.out.println(relevantObjects);

        System.out.println(relevantObjects.size());
    }
}
