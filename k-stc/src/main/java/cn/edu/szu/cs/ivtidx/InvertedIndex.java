package cn.edu.szu.cs.ivtidx;

import cn.edu.szu.cs.entity.Coordinate;

import java.util.*;

public interface InvertedIndex<T> {


    PriorityQueue<T> getSList(List<String> keywords, Coordinate coordinate, double maxDistance, Comparator<T> comparator);

    Map<String,List<String>> getAll();
}
