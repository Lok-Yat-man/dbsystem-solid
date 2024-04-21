package cn.edu.szu.cs.entity;

import cn.edu.szu.cs.kstc.RelevantObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *  适用与DBSCAN算法的相关对象。包含对象的id、坐标、名称、标签等信息的同时，还包含了文本权重信息。
 *  <p> Relevant object for DBSCAN algorithm. In addition to the object's id, coordinates, name, and labels, it also contains text weight information.
 * @author Whitence
 * @date 2024/4/5 20:07
 * @version 1.0
 */
public class DbScanRelevantObject implements Serializable, RelevantObject,Comparable<DbScanRelevantObject>{

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
    /**
     * weights
     * 标签文本权重
     */
    private Map<String,Double> weights = null;

    public DbScanRelevantObject() {
    }

    public DbScanRelevantObject(String objectId, double[] coordinate, String name, List<String> labels) {
        this.objectId = objectId;
        this.coordinate = coordinate;
        this.name = name;
        this.labels = labels;
    }

    private DbScanRelevantObject(String objectId, double[] coordinate, String name, List<String> labels, Map<String, Double> weights) {
        this.objectId = objectId;
        this.coordinate = coordinate;
        this.name = name;
        this.labels = labels;
        this.weights = weights;
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

    public Double getWeight(String label) {
        if(weights == null) {
            return 0.0;
        }
        return weights.getOrDefault(label,0.0);
    }


    public Double getWeight(List<String> labels) {
        if(weights == null) {
            return 0.0;
        }
        return labels.stream().mapToDouble(weights::get).filter(Objects::nonNull).sum();
    }

    public void setWeight(String label, Double weight) {
        if(weights == null) {
            weights = new HashMap<>(16);
        }
        weights.put(label,weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DbScanRelevantObject that = (DbScanRelevantObject) o;
        return objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId);
    }


    @Override
    public int compareTo(DbScanRelevantObject o) {
        return this.objectId.compareTo(o.objectId);
    }
}
