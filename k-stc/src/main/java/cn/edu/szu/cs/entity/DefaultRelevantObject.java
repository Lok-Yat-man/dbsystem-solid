package cn.edu.szu.cs.entity;

import cn.edu.szu.cs.kstc.RelevantObject;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *  默认相关对象。包含对象的id、坐标、名称、标签等信息的
 *  <p> Default relevant object. Contains the id, coordinates, name, labels, etc. of the object.
 * @author Whitence
 * @date 2024/4/5 20:07
 * @version 1.0
 */
public class DefaultRelevantObject implements Serializable, RelevantObject,Comparable<DefaultRelevantObject>{

    /**
     * object id
     * 对象id
     */
    private String objectId;
    /**
     * coordinate
     * 坐标
     */
    private double[] coordinate;
    /**
     * name
     * 名称
     */
    private String name;
    /**
     * labels
     * 标签
     */
    private List<String> labels;

    public DefaultRelevantObject() {
    }

    public DefaultRelevantObject(String objectId, double[] coordinate, String name, List<String> labels) {
        this.objectId = objectId;
        this.coordinate = coordinate;
        this.name = name;
        this.labels = labels;
    }

    @Override
    public String getObjectId() {
        return objectId;
    }

    @Override
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public double[] getCoordinate() {
        return coordinate;
    }

    @Override
    public void setCoordinate(double[] coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<String> getLabels() {
        return labels;
    }

    @Override
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultRelevantObject that = (DefaultRelevantObject) o;
        return objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId);
    }


    @Override
    public int compareTo(DefaultRelevantObject o) {
        return this.objectId.compareTo(o.objectId);
    }
}
