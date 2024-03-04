package cn.edu.szu.cs.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Whitence
 * @date 2023/9/30 22:29
 * @version 1.0
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class KSTCQuery implements Serializable {

    /**
     * coordinate
     * eg. [112,23]
     */
    private double[] coordinate;

    /**
     * keywords
     * eg. ["Water"]
     */
    private List<String> keywords;

    /**
     * top-k
     * eg. 5
     */
    private int k;

    /**
     * epsilon
     * eg. 50.0
     */
    private double epsilon;

    /**
     * min points
     * eg. 5
     */
    private int minPts;


    /**
     * maxDistance
     * eg. 1000.0
     */
    private double maxDistance;

}
