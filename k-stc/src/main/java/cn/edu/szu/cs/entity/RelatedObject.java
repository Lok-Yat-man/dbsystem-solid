package cn.edu.szu.cs.entity;

import cn.edu.szu.cs.entity.Coordinate;

import java.util.List;

/**
 *  RelatedObject
 * @author Whitence
 * @date 2023/11/1 21:53
 * @version 1.0
 */
public interface RelatedObject {

   String getObjectId();

   double[] getCoordinate();

   String getName();

   List<String> getLabels();

   Double getWeight(String label);

   Double setWeight(String label);

   RelatedObject clone();

}
