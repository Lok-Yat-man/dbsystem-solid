package ivtidx;


import com.alibaba.fastjson.JSONObject;
import entity.Pair;
import entity.RelevantObject;
import lombok.Getter;
import service.IRelevantObjectService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class DefaultLeafInvertedIndex implements InvertedIndex<RelevantObject> {

    private static final int MAX_SIZE = 1000;

    private Map<String, List<Pair>> map;
    private IRelevantObjectService relevantObjectService;


    public DefaultLeafInvertedIndex(IRelevantObjectService relevantObjectService) {

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("iFileSkyline.txt");

        if (resourceAsStream == null) {
            throw new RuntimeException("iFileSkyline.txt not exists!");
        }
        assert relevantObjectService != null;

        this.relevantObjectService = relevantObjectService;

        map = new ConcurrentHashMap<>();

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))
        ) {

            String str = "";
            while ((str = reader.readLine()) != null) {
                String listStr = reader.readLine();

                List<Pair> pairs = JSONObject.parseArray(listStr, Pair.class);
                map.put(
                        str,
                        pairs
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public RelevantObject getValue(String s) {
        return relevantObjectService.getById(s);
    }

    @Override
    public List<RelevantObject> getValues(String s) {

        return relevantObjectService.getByIds(
                map.getOrDefault(s, new ArrayList<>())
                        .stream().map(Pair::getKey).collect(Collectors.toList())
        );
    }

    @Override
    public List<RelevantObject> getValues(List<String> ss) {
        return Optional.ofNullable(
                ss
        ).map(
                kwds -> kwds.stream()
                        .map(this::getValues)
                        .reduce(
                                new ArrayList<>(),
                                (a, b) -> {
                                    a.addAll(b);
                                    return a;
                                }
                        )
        ).orElse(new ArrayList<>());
    }

    @Override
    public List<Pair> getMapList(String term) {
        return map.get(term);
    }
}
