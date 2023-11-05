package cn.edu.szu.cs;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class KstcResult<T> implements Serializable {

    private int code;

    private String msg;

    private List<Set<T>> data;

}
