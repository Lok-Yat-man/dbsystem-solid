import com.github.davidmoten.rtree.geometry.Geometry;
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

public class BSTDTest {
    private BSTD b = new BSTD();
    @Test
    public void bstdTest(){
        LinkedList<Query> queries = new LinkedList<>();
        // 边界值：
        // x1 = -120.09514 y1 = 27.555126
        // x2 = -73.200455 y2 = 53.679195

        Query query1 = Query.create(
                Coordinate.create(
                        -75.16256713867188,
                        39.94322204589844
                ),
                Arrays.asList("Restaurants"),
                //Arrays.asList("Water"),
                5,
                60.0,
                3
        );
/*
        Query query2 = Query.create(
                Coordinate.create(
                        -75.1,
                        40.1
                ),
                Arrays.asList("Balloons"),
                //Arrays.asList("Water"),
                5,
                60.0,
                3
        );
*/

        queries.add(query1);
        //queries.add(query2);


        List<Entry<String, Geometry>> valuesEntry = b.bstd(queries);

        List<String> values = valuesEntry.stream()
                .map(Entry::value)
                .collect(Collectors.toList());


        //System.out.println(valuesEntry);
        System.out.println(valuesEntry.size());

        IRelevantObjectService relevantObjectService = new DefaultRelevantObjectServiceImpl();
        System.out.println(relevantObjectService.getByIds(values));
    }
}
