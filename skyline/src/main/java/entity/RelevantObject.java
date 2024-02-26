package entity;

import util.TfIdfStrategy;

import java.io.Serializable;
import java.util.*;

/**
 * 相关对象
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/10/1 10:31
 */
public class RelevantObject implements Serializable {

    private String objectId;

    private Coordinate coordinate;

    private Map<String, Double> weights;

    public RelevantObject() {
    }

    public RelevantObject(String objectId, Coordinate coordinate, List<String> keywords) {
        this.coordinate = coordinate;
        this.objectId = objectId;
        weights = new TfIdfStrategy().calculateWeight(keywords);
    }


    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public void setWeights(Map<String, Double> weights) {
        this.weights = weights;
    }

    public double getWeights(List<String> keywords) {

        if (keywords == null || keywords.isEmpty()) {
            return 0;
        }
        return keywords.stream().map(weights::get).reduce(0.0, Double::sum);
    }

    public List<String> getWeightKey() {

        return new LinkedList<>(weights.keySet());
    }

    public String getObjectId() {
        return objectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelevantObject object = (RelevantObject) o;
        return Objects.equals(coordinate, object.coordinate) && Objects.equals(weights, object.weights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate, weights);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public Map<String, Double> getWeights() {
        return weights;
    }

    @Override
    public String toString() {
        return "RelevantObject{" +
                "objectId='" + objectId + '\'' +
                ", coordinate=" + coordinate +
                ", weights=" + weights +
                '}' + '\n';
    }
}
