package ivtidx;

import entity.Coordinate;
import entity.Pair;

import java.util.*;

public interface InvertedIndex<T> {

    T getValue(String s);
    List<T> getValues(String s);
    List<T> getValues(List<String> ss);
    List<Pair> getMapList(String term);
}
