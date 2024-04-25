import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import entity.Coordinate;
import entity.RelevantObject;
import util.TfIdfStrategy;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputBusiness {
    public static void main(String[] args) {
        List<RelevantObject> relevantObjects = new LinkedList<>();
        try {
            FileReader fileReader = new FileReader("D:\\yelp_academic_dataset_business.json");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String s = "";
            int k = 0;

            while ((s = bufferedReader.readLine()) != null) {
                String json = s;

                JSONObject jsonObject = JSONObject.parseObject(json);

                String businessId = jsonObject.getString("business_id");
                String name = jsonObject.getString("name");
                double longitude = jsonObject.getDoubleValue("longitude");
                double latitude = jsonObject.getDoubleValue("latitude");
                String categories = jsonObject.getString("categories");
                if (categories != null) {
                    List<String> words = new ArrayList<>();
                    // 使用正则表达式匹配非英文字母之间的英文字母的组合
                    Pattern pattern = Pattern.compile("[a-zA-Z]+");
                    Matcher matcher = pattern.matcher(categories);
                    // 将匹配到的单词添加到列表中
                    while (matcher.find()) {
                        if (!words.contains(matcher.group())) {
                            words.add(matcher.group());
                        }
                    }

                    Coordinate coordinate = Coordinate.create(longitude, latitude);
                    RelevantObject relevantObject = new RelevantObject(businessId, name, coordinate, words);
                    relevantObjects.add(relevantObject);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        TfIdfStrategy tfIdfStrategy = new TfIdfStrategy();
        tfIdfStrategy.calculate(relevantObjects);
        System.out.println(relevantObjects.size());

        // 将每个对象转换为 JSON 字符串并写入文件
        try (FileWriter writer = new FileWriter("D:\\dbsystem-solid\\skyline\\src\\main\\resources\\objsSkyline.txt")) {
            for (RelevantObject relevantObject : relevantObjects) {
                String json = JSON.toJSONString(relevantObject);
                writer.write(json + "\n"); // 每个对象单独写一行
            }
            System.out.println("Successfully wrote JSON to file: " + "objsSkyline.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //
//        try (FileWriter writer = new FileWriter("iFile.txt")) {
//            Map<String, Map<String, Double>> invertedIndex  = new HashMap<>();
//
//            for (RelevantObject obj : relevantObjects) {
//                String objectId = obj.getObjectId();
//                Map<String, Double> weights = obj.getWeights();
//
//                // 遍历每个 RelevantObject 对象的 weights 属性
//                for (Map.Entry<String, Double> entry : weights.entrySet()) {
//                    String word = entry.getKey();
//                    Double weight = entry.getValue();
//
//                    // 如果倒排索引中已经存在该词项，则添加到 posting list 中；否则，创建一个新的 posting list
//                    if (invertedIndex.containsKey(word)) {
//                        Map<String, Double> postingList = invertedIndex.get(word);
//                        postingList.put(objectId, weight);
//                    } else {
//                        Map<String, Double> postingList = new HashMap<>();
//                        postingList.put(objectId, weight);
//                        invertedIndex.put(word, postingList);
//                    }
//                }
//            }
//
//            String json = JSON.toJSONString(invertedIndex);
//            writer.write(json);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
