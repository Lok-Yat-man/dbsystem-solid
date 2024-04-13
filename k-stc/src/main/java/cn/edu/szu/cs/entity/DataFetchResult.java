package cn.edu.szu.cs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataFetchResult<T> implements Serializable {

    public static final int SUCCESS = 2000;
    public static final int RUNNING = 2002;
    public static final int FAIL = 2004;

    private String actionId;

    private String commandName;

    private int code;

    private String msg;

    private T data;

    public static <T> DataFetchResult<T> success(String actionId, String commandName, T data) {
        return new DataFetchResult<>(actionId,commandName,SUCCESS, "success", data);
    }

    public static <T> DataFetchResult<T> running(String actionId, String commandName, T data) {
        return new DataFetchResult<>(actionId,commandName,RUNNING, "running", data);
    }

    public static <T> DataFetchResult<T> fail(String actionId, String commandName, String errorMsg) {
        return new DataFetchResult<>(actionId,commandName,FAIL, errorMsg, null);
    }

    public boolean isSuccess() {
        return code == SUCCESS;
    }
}
