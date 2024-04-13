package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.entity.KstcQuery;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 *  记录算法运行过程中的上下文信息
 *  <p> Record the context information during the operation of the algorithm.
 * @author Whitence
 * @date 2024/4/6 11:01
 * @version 1.0
 */
@Data
public class Context<T> {

    /**
     * 查询对象
     * <p> query object
     */
    private KstcQuery query;

    /**
     * 簇
     * <p> clusters
     */
    private List<Set<T>> clusters;

}
