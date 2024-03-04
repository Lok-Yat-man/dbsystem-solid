package cn.edu.szu.cs.ivtidx;

import cn.edu.szu.cs.entity.Coordinate;

import java.util.*;

public interface InvertedIndex<T> {


    SortedSet<T> getSList(List<String> keywords, double[] coordinate, double maxDistance, Comparator<T> comparator);

    SortedSet<T> getTList(List<String> keywords);

    Map<String,Set<String>> getAll();
}
