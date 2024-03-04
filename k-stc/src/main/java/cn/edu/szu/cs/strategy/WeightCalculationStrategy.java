package cn.edu.szu.cs.strategy;

import java.util.List;

/**
 *
 */
public interface WeightCalculationStrategy<T> {

    Double calculate(T obj);

    Double calculate(List<T> objs);

}
