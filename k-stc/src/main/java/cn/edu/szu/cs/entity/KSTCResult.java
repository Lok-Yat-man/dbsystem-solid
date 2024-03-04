package cn.edu.szu.cs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KSTCResult<T> implements Serializable {

    public static final int SUCCESS = 2000;
    public static final int RUNNING = 2002;
    public static final int FAIL = 2004;

    private int code;

    private String msg;

    private List<Set<T>> clusters;


    public static <T> KSTCResult<T> success(List<Set<T>> data) {
        return new KSTCResult<>(SUCCESS, "success", data);
    }

    public static <T> KSTCResult<T> running(List<Set<T>> data) {
        return new KSTCResult<>(RUNNING, "Request is computing.", data);
    }

    public static <T> KSTCResult<T> fail() {
        return new KSTCResult<>(FAIL, "fail", null);
    }

    public static <T> KSTCResult<T> fail(String errorMsg) {
        return new KSTCResult<>(FAIL, errorMsg, null);
    }

}
