package cn.edu.szu.cs.strategy;

import java.util.List;

/**
 *
 */
public interface WeightCalculationStrategy<T> {

    void calculate(List<T> objects);

}
