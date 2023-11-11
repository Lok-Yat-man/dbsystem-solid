package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.entity.Query;

import java.util.List;
import java.util.Set;

public interface KSTC<T> {

    List<Set<T>> kstcSearch(Query query);
}
