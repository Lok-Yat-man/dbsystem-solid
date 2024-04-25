package util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import entity.RelevantObject;

import java.util.*;


//public class TfIdfStrategy implements WeightCalculationStrategy {
//    @Override
//    public Map<String, Double> calculateWeight(List<String> keywords) {
//
//        Map<String, Double> weights = new HashMap<>();
//
//        int len = keywords.size();
//
//        if (len == 1) {
//            weights.put(keywords.get(0), 1.0);
//        } else {
//
//            // calculate tf-idf weight
//            Map<String, Integer> tfCnt = new HashMap<>(len);
//
//            for (String keyword : keywords) {
//                tfCnt.put(keyword, tfCnt.getOrDefault(keyword, 0) + 1);
//            }
//
//            for (String keyword : tfCnt.keySet()) {
//                int cnt = tfCnt.get(keyword);
//                double a = (double) cnt / (double) len;
//                double b = Math.log((double) len / (double) (cnt));
//                double wei = a * b;
//                weights.put(keyword, wei);
//            }
//        }
//        return weights;
//    }
//}
public class TfIdfStrategy {

    private static final Log log = LogFactory.get();

    public void calculate(List<RelevantObject> objects) {
        if (objects == null || objects.isEmpty()) {
            return;
        }
        log.debug("Start to calculate TF-IDF");
        // idf
        Map<String, Set<String>> wordDocsMap = new HashMap<>();
        for (RelevantObject object : objects) {
            if (object.getLabels() == null || object.getLabels().isEmpty()) {
                continue;
            }
            for (String label : object.getLabels()) {
                if (StrUtil.isBlank(label)) {
                    continue;
                }
                wordDocsMap.putIfAbsent(label, new HashSet<>());
                wordDocsMap.get(label).add(object.getObjectId());
            }
        }
        int docNum = objects.size();
        Map<String, Double> idf = new HashMap<>();
        wordDocsMap.forEach((k, v) -> idf.put(k, Math.log(docNum / ((double) v.size()))));
        // calculate total words
        double totalWords = 0.0;
        for (RelevantObject object : objects) {
            if (object.getLabels() == null || object.getLabels().isEmpty()) {
                continue;
            }
            for (String label : object.getLabels()) {
                if (StrUtil.isBlank(label)) {
                    continue;
                }
                totalWords += 1.0;
            }
        }
        for (RelevantObject object : objects) {
            if (object.getLabels() == null || object.getLabels().isEmpty()) {
                continue;
            }
            // calculate word frequency
            Map<String, Double> tf = new HashMap<>();
            for (String label : object.getLabels()) {
                if (StrUtil.isBlank(label)) {
                    continue;
                }
                tf.put(label, tf.getOrDefault(label, 0.0) + 1);
            }

            // tf-idf
            double finalTotalWords = totalWords;
            tf.forEach((label, v) -> {
                double tfVal = v / finalTotalWords;
                double idfVal = idf.getOrDefault(label, 0.0);
                object.setWeight(label, tfVal * idfVal);
            });

            //normalize
            double sum = 0.0;
            for (String label : object.getLabels()) {
                sum += object.getWeight(label);
            }
            for (String label : object.getLabels()) {
                if (object.getWeight(label) / sum > 1) {
                    object.setWeight(label, 1.0);
                } else {
                    object.setWeight(label, object.getWeight(label) / sum);
                }
            }
        }

        log.debug("TF-IDF calculation finished.");
    }

}
