package cn.edu.szu.cs.strategy.impl;

import cn.edu.szu.cs.entity.DefaultRelatedObject;
import cn.hutool.core.lang.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

class TfIdfStrategyTest {
    TfIdfStrategy<DefaultRelatedObject> tfIdfStrategy = new TfIdfStrategy<>();

    @Test
    void testCalculate() {

        List<DefaultRelatedObject> relatedObjects = List.of(
                new DefaultRelatedObject("1",new double[]{1,1},"a",List.of("The", "sky", "is", "blue", "and", "beautiful")),
                new DefaultRelatedObject("2",new double[]{1,1},"b",List.of("The", "sun", "is", "bright", "and", "the", "sky", "is", "blue")),
                new DefaultRelatedObject("3",new double[]{1,1},"c",List.of("The", "sun", "in", "the", "sky", "is", "bright"))
        );

        tfIdfStrategy.calculate(relatedObjects);

        Assert.equals(relatedObjects.get(0).getWeight("and"), 0.018430232186734747);
        Assert.equals(relatedObjects.get(0).getWeight("beautiful"), 0.04993692221218681);
        Assert.equals(relatedObjects.get(0).getWeight("blue"), 0.018430232186734747);
        Assert.equals(relatedObjects.get(0).getWeight("is"), 0.0);
        Assert.equals(relatedObjects.get(0).getWeight("sky"), 0.0);


    }

    private boolean equals(Double a, Double b) {
        return Math.abs(a - b) < 1e-4;
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme