package cn.edu.szu.cs;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface KSTC<T> {

    List<Set<T>> kstcSearch(Query query);
}
