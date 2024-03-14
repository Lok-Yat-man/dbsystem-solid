package cn.edu.szu.cs.fatory;

import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;

public interface KSTCFactory<T> {

    TopKSpatialTextualClustersRetrieval<T> createTopKSpatialTextualClustersRetrieval();

}
