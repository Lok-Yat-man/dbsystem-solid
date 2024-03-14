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

   Double getWeight(List<String> labels);

   void setWeight(String label,Double weight);

   RelatedObject clone();

   @Override
   int hashCode();

   @Override
   boolean equals(Object obj);
}
