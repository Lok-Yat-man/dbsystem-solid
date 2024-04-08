package cn.edu.szu.cs.ivtidx;

import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.infrastructure.IRelevantObjectDataLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class DefaultDBScanInvertedIndexTest {
    @Mock
    Map<String, Set<String>> map;
    @Mock
    IRelevantObjectDataLoader<DbScanRelevantObject> relevantLoader;
    @InjectMocks
    DefaultDBScanInvertedIndex defaultDBScanInvertedIndex;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testLoadDataSortByDistanceAsc() {
        when(relevantLoader.getByIds(any())).thenReturn(List.of(new DbScanRelevantObject("objectId", new double[]{0d,0d}, "name", List.of("String"))));

        SortedSet<DbScanRelevantObject> result = defaultDBScanInvertedIndex.loadDataSortByDistanceAsc(List.of("String"), new double[]{0d,0d}, Double.MAX_VALUE);
        Assertions.assertEquals(new TreeSet<>(List.of(new DbScanRelevantObject("objectId", new double[]{0d,0d}, "name", List.of("String")))), result);
    }

    @Test
    void testLoadDataSortByWeightDesc() {
        SortedSet<DbScanRelevantObject> result = defaultDBScanInvertedIndex.loadDataSortByWeightDesc(List.of("String"));
        Assertions.assertEquals(new TreeSet<>(List.of(new DbScanRelevantObject("objectId", new double[]{0d,0d}, "name", List.of("String")))), result);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme