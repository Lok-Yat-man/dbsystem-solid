package com.edu.szu.api;

import java.awt.geom.PathIterator;

public interface Pair {
    public String getStart();

    public String getEnd();

    public static Pair of(String start, String end){
        return new Pair() {
            @Override
            public String getStart() {
                return start;
            }

            @Override
            public String getEnd() {
                return end;
            }
        };
    }
}
