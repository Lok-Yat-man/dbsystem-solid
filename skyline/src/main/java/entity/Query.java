package entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class Query implements Serializable {

    private Coordinate location;

    private List<String> keywords;

    public Query(){}

    public Query(Coordinate location, List<String> keywords) {
        this.location = location;
        this.keywords = keywords;
    }

    public static Query create(Coordinate location, List<String> keywords){
        return new Query(location, keywords);
    }

    @Override
    public String toString() {
        return "Query{" +
                "location=" + location +
                ", keywords=" + keywords +
                '}';
    }
}