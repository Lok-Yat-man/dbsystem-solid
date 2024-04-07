package cn.edu.szu.cs.strategy.impl;

import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.strategy.WeightCalculationStrategy;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import java.util.*;

/**
 * Using TF-IDF algorithm to calculate document vector
 *
 * @author Whitence
 * @version 1.0
 * @date 2024/3/7 23:42
 */

public class TfIdfStrategy<T extends DbScanRelevantObject> implements WeightCalculationStrategy<T> {

    private static final Log log = LogFactory.get();

    @Override
    public void calculate(List<T> objects) {
        if (objects == null || objects.isEmpty()) {
            return;
        }
        log.debug("Start to calculate TF-IDF");
        // idf
        Map<String, Set<String>> wordDocsMap = new HashMap<>();
        for (T object : objects) {
            if (object.getLabels() == null || object.getLabels().isEmpty()) {
                continue;
            }
            for (String label : object.getLabels()) {
                if (StrUtil.isBlank(label)) {
                    continue;
                }
                String labelLower = label.toLowerCase();
                wordDocsMap.putIfAbsent(labelLower, new HashSet<>());
                wordDocsMap.get(labelLower).add(object.getObjectId());
            }
        }
        int docNum = objects.size();
        Map<String, Double> idf = new HashMap<>();
        wordDocsMap.forEach((k, v) -> idf.put(k, Math.log(docNum / ((double) v.size()))));
        // calculate total words
        double totalWords = 0.0;
        for (T object : objects) {
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
        for (T object : objects) {
            if (object.getLabels() == null || object.getLabels().isEmpty()) {
                continue;
            }
            // calculate word frequency
            Map<String, Double> tf = new HashMap<>();
            for (String label : object.getLabels()) {
                if (StrUtil.isBlank(label)) {
                    continue;
                }
                String labelLower = label.toLowerCase();
                tf.put(labelLower, tf.getOrDefault(labelLower, 0.0) + 1);
            }

            // tf-idf
            double finalTotalWords = totalWords;
            tf.forEach((label, v) -> {
                double tfVal = v / finalTotalWords;
                double idfVal = idf.getOrDefault(label, 0.0);
                object.setWeight(label, tfVal * idfVal);
            });

            // normalize
            double sum = 0.0;
            for (String label : object.getLabels()) {
                sum += object.getWeight(label);
            }
            for (String label : object.getLabels()) {
                object.setWeight(label, object.getWeight(label) / sum);
            }

        }

        log.debug("TF-IDF calculation finished.");
    }

}
