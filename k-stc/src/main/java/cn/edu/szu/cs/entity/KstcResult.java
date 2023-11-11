package cn.edu.szu.cs.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class KstcResult<T> implements Serializable {

    private int code;

    private String msg;

    private List<Set<T>> data;


    public KstcResult(int code, String msg, List<Set<T>> data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> KstcResult<T> success(List<Set<T>> data){
        return new KstcResult<>(2000,"success",data);
    }

    public static <T> KstcResult<T> running(){
        return new KstcResult<>(2002,"Request is computing.",null);
    }

    public static <T> KstcResult<T> timeout(){
        return new KstcResult<>(2004,"Request time out.",null);
    }

}
