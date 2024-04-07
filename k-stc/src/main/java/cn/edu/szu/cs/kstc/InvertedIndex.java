package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.entity.RelevantObject;

import java.util.List;

/**
 *  倒排索引。主要提供根据文本关键词获取相关对象的功能
 * @author Whitence
 * @date 2024/4/6 14:30
 * @version 1.0
 */
public interface InvertedIndex<T extends RelevantObject> {

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
}
