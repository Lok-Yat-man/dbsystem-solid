package cn.edu.szu.cs.service;

import java.util.List;

/**
 *  IRelatedObjectService
 * @author Whitence
 * @date 2023/11/1 22:06
 * @version 1.0
 */
public interface IRelatedObjectService<T> {

    /**
     * 通过id获取对象
     * @param id
     * @return
     */
    T getById(String id);

    /**
     * 通过ids获取对象
     * @param ids
     * @return
     */
    List<T> getByIds(List<String> ids);

    /**
     * 通过id获取标签
     * @param id
     * @return
     */
    List<String> getLabelsById(String id);

    /**
     * 获取所有对象
     * @return
     */
    List<T> getAll();

}
