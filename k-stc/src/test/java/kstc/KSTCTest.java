package kstc;

import cn.edu.szu.cs.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class KSTCTest {

    private KSTC kstc = new SimpleKSTC();

    @Test
    public void simpleKSTCTest(){

        Query query = Query.create(
                Coordinate.create(
                        -75.16256713867188,
                        39.94322204589844
                ),
                Arrays.asList("Restaurants"),
                //Arrays.asList("Water"),
                5,
                60.0,
                3
        );

    }

}
