package entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
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
}

