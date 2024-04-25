package entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;


public class RelevantObject implements Serializable {

    @Getter
    @Setter
    private String objectId;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Coordinate coordinate;

    @Getter
    @Setter
    private List<String>  labels;

    @Getter
    @Setter
    private Map<String, Double> weights;

    public RelevantObject() {
    }

    public RelevantObject(String objectId, String name, Coordinate coordinate, List<String> keywords) {
        this.coordinate = coordinate;
        this.labels = keywords;
        this.objectId = objectId;
        this.name = name;
    }

    public Double getWeight(String label) {
        if (weights == null) {
            return 0.0;
        }
        return weights.getOrDefault(label, 0.0);
    }

    public Double getWeight(List<String> labels) {
        if (weights == null) {
            return 0.0;
        }
        return labels.stream().mapToDouble(weights::get).filter(Objects::nonNull).sum();
    }

    public void setWeight(String label, Double weight) {
        if (weights == null) {
            weights = new HashMap<>(16);
        }
        weights.put(label, weight);
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

    @Override
    public String toString() {
        return "RelevantObject{" +
                "objectId='" + objectId + '\'' +
                ", coordinate=" + coordinate +
                ", weights=" + weights +
                '}' + '\n';
    }
}
