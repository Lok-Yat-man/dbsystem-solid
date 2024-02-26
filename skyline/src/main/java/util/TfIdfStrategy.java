package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TfIdfStrategy
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/10/18 23:22
 */
public class TfIdfStrategy implements WeightCalculationStrategy {
    @Override
    public Map<String, Double> calculateWeight(List<String> keywords) {

        Map<String, Double> weights = new HashMap<>();

        int len = keywords.size();

        if (len == 1) {
            weights.put(keywords.get(0), 1.0);
        } else {

            // calculate tf-idf weight
            Map<String, Integer> tfCnt = new HashMap<>(len);

            for (String keyword : keywords) {
                tfCnt.put(keyword, tfCnt.getOrDefault(keyword, 0) + 1);
            }

            for (String keyword : tfCnt.keySet()) {
                int cnt = tfCnt.get(keyword);
                double a = (double) cnt / (double) len;
                double b = Math.log((double) len / (double) (cnt));
                double wei = a * b;
                weights.put(keyword, wei);
            }
        }
        return weights;
    }
}
