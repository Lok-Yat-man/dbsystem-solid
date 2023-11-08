package com.edu.szu.api;

/**
 * @description: TODO
 * @author whitence
 * @date 2023/4/19 21:37
 * @version 1.0
 */
public interface IResult {
    /**
     * 获取结果码
     * @return
     */
    Integer getCode();

    /**
     * 获取消息
     * @return
     */
    String getMessage();
}
