import com.alibaba.fastjson.JSON;
import entity.Coordinate;
import entity.GeoJson;
import entity.Query;
import entity.RelevantObject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import std.BSTD;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class WriteGeoJson {
    private BSTD b = new BSTD();
    @SneakyThrows
    @Test
    public void writeGeoJson() {
        LinkedList<Query> queries = new LinkedList<>();
        Query query2 = Query.create(
                Coordinate.create(
                        -118.26,
                        34.02
                ),
                Arrays.asList("Restaurants")
        );
        queries.add(query2);

        List<RelevantObject> relevantObjectList = b.bstd(queries);
        GeoJson geoJson = new GeoJson();
        for (int i = 0; i < relevantObjectList.size(); i++) {
            RelevantObject relevantObject = relevantObjectList.get(i);
            String skylineId = i + "";
            GeoJson.Geometry geometry = new GeoJson.Geometry(
                    relevantObject.getCoordinate().getLongitude(),
                    relevantObject.getCoordinate().getLatitude()
            );
            GeoJson.Properties properties = new GeoJson.Properties(skylineId);
            GeoJson.Feature feature = new GeoJson.Feature(geometry, properties);
            geoJson.getFeatures().add(feature);
        }

        String jsonString = JSON.toJSONString(geoJson, true);
        FileWriter fileWriter = new FileWriter("America.geojson");
        fileWriter.write(jsonString);
        fileWriter.close();
        System.out.println("GeoJSON file created successfully!");
    }
}
