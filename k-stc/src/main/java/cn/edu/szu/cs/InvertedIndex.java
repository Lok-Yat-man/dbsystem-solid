package cn.edu.szu.cs;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public interface InvertedIndex<T> {
    List<T> getValues(String s);
    List<T> getValues(List<String> ss);

    PriorityQueue<T> getSList(List<String> keyword, Comparator<T> comparator);

}
