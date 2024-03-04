package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.entity.Coordinate;
import cn.edu.szu.cs.entity.KSTCQuery;
import cn.edu.szu.cs.entity.KSTCResult;

import java.util.List;
import java.util.Set;

public interface TopKSpatialTextualClustersRetrieval<T> {

    KSTCResult<T> kstcSearch(KSTCQuery query);

}
