package cn.edu.szu.cs;

import java.util.List;

public interface IRTree<T> {

    List<T> rangeQuery(List<String> keywords,Coordinate coordinate,double epsilon);

}
