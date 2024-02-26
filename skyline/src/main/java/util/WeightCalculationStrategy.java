package util;

import java.util.List;
import java.util.Map;

/**
 * WeightCalculationStrategy
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/10/18 23:18
 */
public interface WeightCalculationStrategy {

    Map<String, Double> calculateWeight(List<String> keywords);

}
