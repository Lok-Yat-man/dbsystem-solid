package cn.edu.szu.cs;

import java.util.List;
import java.util.Set;

public interface KSTC<T> {

    List<Set<T>> kstcSearch(Query query);


}
