package cn.edu.szu.cs.infrastructure.dataloader;

import cn.edu.szu.cs.kstc.RelevantObject;

import java.util.List;

/**
 *  加载相关对象数据接口
 *  <p> Load relevant object data
 * @author Whitence
 * @date 2023/11/1 22:06
 * @version 1.0
 */
public interface IRelevantObjectDataLoader<T extends RelevantObject> {

    /**
     * 通过id获取对象
     * <p> Get object by id
     * @param id
     * @return
     */
    T getById(String id);

    /**
     * 通过ids获取对象
     * <p> Get object by ids
     * @param ids
     * @return
     */

    List<T> getByIds(List<String> ids);

    /**
     * 获取所有对象
     * <p> Get all objects
     * @return
     */

    List<T> getAll();


    /**
     * 通过关键词获取对象
     * <p> Get object by keyword
     * @param keyword
     * @return
     */

    List<T> getObjectsByKeyword(String keyword);

    /**
     * 通过关键词获取对象
     * <p> Get object by keywords
     * @param keywords
     * @return
     */

    List<T> getObjectsByKeywords(List<String> keywords);


    /**
     * 获取所有标签
     * <p> Get all labels
     * @return
     */
    List<String> getAllLabels();

}
