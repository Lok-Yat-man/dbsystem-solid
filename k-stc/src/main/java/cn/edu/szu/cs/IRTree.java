package cn.edu.szu.cs;

import java.util.List;

public interface IRTree {

    List<RelevantObject> rangeQuery(Query query, RelevantObject p);

}
