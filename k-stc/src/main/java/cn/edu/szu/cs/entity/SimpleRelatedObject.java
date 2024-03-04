package cn.edu.szu.cs.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 相关对象
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/10/1 10:31
 */
public class SimpleRelatedObject implements Serializable, RelatedObject {

    private String objectId;

    private double[] coordinate;

    private String name;

    private List<String> labels;

    public SimpleRelatedObject() {
    }

    public SimpleRelatedObject(String objectId, double[] coordinate, String name, List<String> labels) {
        this.objectId = objectId;
        this.coordinate = coordinate;
        this.name = name;
        this.labels = labels;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setCoordinate(double[] coordinate) {
        this.coordinate = coordinate;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String getObjectId() {
        return objectId;
    }

    @Override
    public double[] getCoordinate() {
        return coordinate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getLabels() {
        return labels;
    }

    @Override
    public Double getWeight(String label) {
        return 0.0;
    }

    @Override
    public RelatedObject clone() {
        return new SimpleRelatedObject(objectId, coordinate, name, labels);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleRelatedObject that = (SimpleRelatedObject) o;
        return objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId);
    }
}
