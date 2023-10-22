package cn.edu.szu.cs;

import java.io.Serializable;

public class Pair implements Serializable {

    private String key;

    private Double value;

    private Pair(String key, Double value){
        this.key=key;
        this.value=value;
    }
    Pair(){

    }

    public static Pair create(String key,Double value){
        return new Pair(key,value);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
