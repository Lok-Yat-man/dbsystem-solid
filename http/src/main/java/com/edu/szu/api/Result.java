package com.edu.szu.api;

import lombok.Data;

/**
 * @description: 返回结果类
 * @author whitence
 * @date 2023/4/19 21:45
 * @version 1.0
 */
@Data
public class Result<T> {
    /**
     * 状态码
     * @mock 2000
     */
    private Integer code;
    /**
     * 请求结果
     * @mock 操作成功
     */
    private String message;
    /**
     * 返回数据
     */
    private T data;
    protected Result() {
    }
    protected Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     * @param  message 提示信息
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果
     * @param resultCode 错误码
     */
    public static <T> Result<T> failed(IResult resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败返回结果
     * @param resultCode 错误码
     * @param message 错误信息
     */
    public static <T> Result<T> failed(IResult resultCode,String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    /**
     * 失败返回结果
     * @param message 提示信息
     */
    public static <T> Result<T> failed(String message) {
        return new Result<>(ResultCode.FAILED.getCode(), message, null);
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> failed() {
        return failed(ResultCode.FAILED);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> Result<T> validateFailed() {
        return failed(ResultCode.VALIDATE_FAILED);
    }


    /**
     * 请求超时
     */
    public static <T> Result<T> timeout() {
        return failed(ResultCode.TIME_OUT);
    }


    /**
     * 参数验证失败返回结果
     * @param message 提示信息
     */
    public static <T> Result<T> validateFailed(String message) {
        return new Result<>(ResultCode.VALIDATE_FAILED.getCode(), message, null);
    }


    /**
     * 未授权返回结果
     */
    public static <T> Result<T> forbidden(T data) {
        return new Result<>(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage(), data);
    }

}
