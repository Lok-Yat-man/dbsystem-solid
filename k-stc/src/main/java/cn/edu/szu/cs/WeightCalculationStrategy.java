package cn.edu.szu.cs;

import java.util.List;
import java.util.Map;
/**
 *  WeightCalculationStrategy
 * @author Whitence
 * @date 2023/10/18 23:18
 * @version 1.0
 */
public interface WeightCalculationStrategy {

    Map<String,Double> calculateWeight(List<String> keywords) ;

}
