package com.edu.szu.api;
/**
 * @description: 结果码
 * @author whitence
 * @date 2023/4/19 21:42
 * @version 1.0
 */
public enum ResultCode implements IResult {
    // success
    SUCCESS(2000, "请求成功"),
    // unauthorized
    TIME_OUT(2001, "请求超时"),
    // validate_failed
    VALIDATE_FAILED(2004, "参数检验失败"),
    // forbidden,
    FORBIDDEN(2003, "没有相关权限"),
    // failed
    FAILED(2005, "请求失败");
    /**
     * code
     */
    private final Integer code;
    /**
     * message
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    @Override
    public Integer getCode() {
        return code;
    }
    @Override
    public String getMessage() {
        return message;
    }
}