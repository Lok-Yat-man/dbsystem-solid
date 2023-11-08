package cn.edu.szu.cs;

import java.util.*;

public interface InvertedIndex<T> {

    PriorityQueue<T> getSList(List<String> keywords,Coordinate coordinate,double maxDistance,  Comparator<T> comparator);
    List<String> keys();
}
