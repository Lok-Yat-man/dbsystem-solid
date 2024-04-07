package cn.edu.szu.cs.strategy.impl;

import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.hutool.core.lang.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

class TfIdfStrategyTest {
    TfIdfStrategy<DbScanRelevantObject> tfIdfStrategy = new TfIdfStrategy<>();

    @Test
    void testCalculate() {

        List<DbScanRelevantObject> relatedObjects = List.of(
                new DbScanRelevantObject("1",new double[]{1,1},"a",List.of("The", "sky", "is", "blue", "and", "beautiful")),
                new DbScanRelevantObject("2",new double[]{1,1},"b",List.of("The", "sun", "is", "bright", "and", "the", "sky", "is", "blue")),
                new DbScanRelevantObject("3",new double[]{1,1},"c",List.of("The", "sun", "in", "the", "sky", "is", "bright"))
        );

        tfIdfStrategy.calculate(relatedObjects);

        Assert.equals(relatedObjects.get(0).getWeight("and"), 0.21233625702021347);
        Assert.equals(relatedObjects.get(0).getWeight("beautiful"), 0.5753274859595731);
        Assert.equals(relatedObjects.get(0).getWeight("blue"), 0.21233625702021347);
        Assert.equals(relatedObjects.get(0).getWeight("is"), 0.0);
        Assert.equals(relatedObjects.get(0).getWeight("sky"), 0.0);


    }

    private boolean equals(Double a, Double b) {
        return Math.abs(a - b) < 1e-4;
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme