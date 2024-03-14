package cn.edu.szu.cs.ivtidx;

import cn.edu.szu.cs.entity.Coordinate;

import java.util.*;

public interface InvertedIndex<T> {


    SortedMap<T, Boolean> getSList(List<String> keywords, double[] coordinate, double maxDistance, Comparator<T> comparator);

    SortedMap<T, Boolean> getTList(List<String> keywords);

    Map<String, Set<String>> getAll();
}
