package cn.edu.szu.cs.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 空间文本聚类查询参数
 * Spatial text clustering query parameters
 * @author Whitence
 * @date 2023/9/30 22:29
 * @version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class KstcQuery extends BaseDataFetchActionParams implements Serializable {

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
