package cn.edu.szu.cs.entity;

import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DefaultRelatedObject implements Serializable, RelatedObject {

    @Setter
    private String objectId;
    @Setter
    private double[] coordinate;
    @Setter
    private String name;
    @Setter
    private List<String> labels;

    private Map<String,Double> weights = new HashMap<>();

    public DefaultRelatedObject() {
    }

    public DefaultRelatedObject(String objectId, double[] coordinate, String name, List<String> labels, Map<String, Double> weights) {
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
        return weights.getOrDefault(label,0.0);
    }

    @Override
    public Double setWeight(String label) {
        return null;
    }

    @Override
    public RelatedObject clone() {
        return new DefaultRelatedObject(objectId,coordinate,name,labels,weights);
    }

}
