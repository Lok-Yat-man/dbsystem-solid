package cn.edu.szu.cs.ds.irtree;

import cn.edu.szu.cs.entity.Coordinate;

import java.util.List;

public interface IRTree<T> {

    List<T> rangeQuery(List<String> keywords, Coordinate coordinate, double epsilon);

}
