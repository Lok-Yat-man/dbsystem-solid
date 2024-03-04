package cn.edu.szu.cs.strategy;

import cn.edu.szu.cs.entity.KSTCQuery;
import cn.edu.szu.cs.entity.KSTCResult;

import java.util.List;
import java.util.Set;

public interface CacheStrategy<T> {

    void set(KSTCQuery query, KSTCResult<T> kstcResult);

    KSTCResult<T> get(KSTCQuery query);
}
