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

    public static QueryBuilder builder(){
        return new QueryBuilder();
    }

    public static class QueryBuilder{

        private Query query;

        public QueryBuilder(){
            query=new Query();
        }
        public QueryBuilder(Query query){
            this.query=query;
        }

        public QueryBuilder location(Coordinate coordinate){
            query.setLocation(coordinate);
            return this;
        }

        public QueryBuilder keyword(List<String> kwds){
            query.setKeywords(kwds);
            return this;
        }

        public Query build(){
            return query;
        }
    }

    @Override
    public String toString() {
        return "Query{" +
                "location=" + location +
                ", keywords=" + keywords +
                '}';
    }
}