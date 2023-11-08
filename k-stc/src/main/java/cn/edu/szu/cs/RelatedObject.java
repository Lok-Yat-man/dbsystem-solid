package cn.edu.szu.cs;

import java.util.List;

/**
 *  RelatedObject
 * @author Whitence
 * @date 2023/11/1 21:53
 * @version 1.0
 */
public interface RelatedObject {

   String getObjectId();

   Coordinate getCoordinate();

   String getName();

   List<String> getLabels();

}
