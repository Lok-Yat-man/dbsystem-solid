package util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import entity.RelevantObject;

import java.util.*;

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
                object.setWeight(label, object.getWeight(label) / sum);
            }
        }

        log.debug("TF-IDF calculation finished.");
    }

}
