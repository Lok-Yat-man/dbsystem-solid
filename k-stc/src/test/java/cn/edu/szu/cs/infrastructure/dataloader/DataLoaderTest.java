package cn.edu.szu.cs.infrastructure.dataloader;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.entity.DefaultRelevantObject;

import java.util.stream.Collectors;

public class DataLoaderTest {
    public static void main(String[] args) {
        IRelevantObjectDataLoader<DefaultRelevantObject> dataLoader = KstcDataFetchManager.getDataLoader();

        dataLoader.getAllLabels()
                .stream()
                .collect(Collectors.toMap(label -> label, label -> dataLoader.getObjectsByKeyword(label).size()))
                .entrySet()
                .stream()
                .sorted((o1, o2) -> o2.getValue() - o1.getValue())
                .limit(10)
                .forEach(System.out::println);
    }
}
