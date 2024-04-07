package cn.edu.szu.cs.common;

import cn.edu.szu.cs.entity.BaseDataFetchActionParams;

/**
 *  Cacheable
 * @author Whitence
 * @date 2024/4/7 21:22
 * @version 1.0
 */
public interface Cacheable<T extends BaseDataFetchActionParams> {
    /**
     * 获取缓存键
     * <p> Get cache key
     * @return
     * @param params
     */
    String getCacheKey(T params);
}
