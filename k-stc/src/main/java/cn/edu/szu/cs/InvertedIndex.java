package cn.edu.szu.cs;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public interface InvertedIndex<T> {

    PriorityQueue<T> getSList(List<String> keywords,Coordinate coordinate,double maxDistance,  Comparator<T> comparator);

    List<String> keys();
}
