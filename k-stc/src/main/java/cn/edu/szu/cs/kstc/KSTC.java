package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.entity.KSTCQuery;

import java.util.List;
import java.util.Set;

public interface KSTC<T> {

    List<Set<T>> kstcSearch(KSTCQuery KSTCQuery);
}
