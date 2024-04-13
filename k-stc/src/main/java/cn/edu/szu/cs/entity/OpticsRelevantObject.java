package cn.edu.szu.cs.entity;

import cn.edu.szu.cs.kstc.RelevantObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * OpticsRelevantObject
 *
 * @author Whitence
 * @version 1.0
 * @date 2024/4/5 12:00
 */
@AllArgsConstructor
@NoArgsConstructor
public class OpticsRelevantObject implements RelevantObject, Serializable, Comparable<OpticsRelevantObject> {

    @Setter
    private String objectId;
    @Setter
    private double[] coordinate;
    @Setter
    private String name;
    @Setter
    private List<String> labels;

    @Setter
    @Getter
    private Double reachableDistance = Double.MAX_VALUE;

    @Setter
    @Getter
    private Double coreDistance = Double.MAX_VALUE;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpticsRelevantObject that = (OpticsRelevantObject) o;
        return objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId);
    }

    @Override
    public int compareTo(OpticsRelevantObject o) {
        return this.objectId.compareTo(o.objectId);
    }
}
