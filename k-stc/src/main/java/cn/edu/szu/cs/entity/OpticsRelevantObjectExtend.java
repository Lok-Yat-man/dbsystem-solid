package cn.edu.szu.cs.entity;

import lombok.Data;

import java.io.Serializable;
/**
 *  Optics相关对象扩展类
 *  <p> Optics relevant object extension class
 * @author Whitence
 * @date 2024/4/19 10:47
 * @version 1.0
 */
@Data
public class OpticsRelevantObjectExtend implements Serializable {

    /**
     * 对象ID
     * <p> object ID
     */
    private String objectId;
    /**
     * 可达距离
     * <p> reachable distance
     */
    private Double reachableDistance = null;

    /**
     * 核心距离
     * <p> core distance
     */
    private Double coreDistance = null;

}
