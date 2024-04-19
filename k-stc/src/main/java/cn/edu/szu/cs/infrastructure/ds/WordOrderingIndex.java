package cn.edu.szu.cs.infrastructure.ds;

import cn.edu.szu.cs.entity.GeoPointDouble;
import cn.edu.szu.cs.entity.OpticsRelevantObject;
import cn.edu.szu.cs.util.CommonUtil;
import cn.edu.szu.cs.util.TimerHolder;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.github.davidmoten.rtree.Entries;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WordOrderingIndex {

    private static final int minPts = 5;

    private static final double eps = 200;

    private static Log log = LogFactory.get();

    private InputStream inputStream;


    private LRUCache<String, List<OpticsRelevantObject>> wordOrderingIndex;


    public WordOrderingIndex(InputStream inputStream) {
        this.inputStream = inputStream;
        wordOrderingIndex = new LRUCache<>(128, 60 * 60 * 1000);
    }


    public synchronized List<OpticsRelevantObject> getOrderingData(String label) {

        if (wordOrderingIndex.containsKey(label)) {
            return wordOrderingIndex.get(label);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.length() == label.length() && line.equals(label)) {
                    String jsonStr = reader.readLine();
                    List<OpticsRelevantObject> objects = JSON.parseArray(jsonStr, OpticsRelevantObject.class);
                    wordOrderingIndex.put(label, objects);
                    return objects;
                }
            }
        } catch (IOException e) {
            log.error("读取文件失败", e);
        }
        return Collections.emptyList();
    }


    public static void generateWordOrderingIndexFile(InputStream inputStream, OutputStream outputStream) {

        log.info("开始加载数据...");
        TimerHolder.start("total");

        Map<String, Set<OpticsRelevantObject>> labelObjectsMap = new ConcurrentHashMap<>();

        IoUtil.readLines(
                new InputStreamReader(inputStream),
                new LineHandler() {
                    @Override
                    public void handle(String line) {
                        OpticsRelevantObject object = JSON.parseObject(line, OpticsRelevantObject.class);
                        List<String> labels = object.getLabels();
                        if (CollUtil.isNotEmpty(labels)) {
                            for (String label : labels) {
                                labelObjectsMap.putIfAbsent(label, new HashSet<>());
                                labelObjectsMap.get(label).add(object);
                            }
                        }
                    }
                }
        );

        Map<String, List<OpticsRelevantObject>> tmpWordOrderingIndex = new ConcurrentHashMap<>();

        log.info("数据加载完成，开始处理数据...");
        log.info("标签数量：{}", labelObjectsMap.size());
        int cnt = 0;
        for (Map.Entry<String, Set<OpticsRelevantObject>> stringSetEntry : labelObjectsMap.entrySet()) {
            String label = stringSetEntry.getKey();
            Set<OpticsRelevantObject> objects = stringSetEntry.getValue();
            log.info("正在处理标签：{},数据量：{}", label, objects.size());
            TimerHolder.start("wordOrderingIndex");
            List<OpticsRelevantObject> opticsRelevantObjects = orderGenerate(new ArrayList<>(objects));
            long timestamp = TimerHolder.stop("wordOrderingIndex");
            log.info("处理成功标签：{}，数据量：{},耗时：{} ms", label, opticsRelevantObjects.size(), timestamp);
            tmpWordOrderingIndex.put(label, opticsRelevantObjects);
            cnt++;
            log.info("已处理标签数量：{},总标签数：{}", cnt, labelObjectsMap.size());
        }

        log.info("数据处理完成，开始写入文件...");
        TimerHolder.start("writeFile");
        PrintWriter printWriter = new PrintWriter(outputStream);

        tmpWordOrderingIndex.forEach((label, objects) -> {
            printWriter.println(label);
            printWriter.println(JSON.toJSONString(objects));
        });
        long writeFile = TimerHolder.stop("writeFile");
        log.info("写入文件完成...耗时：{} ms", writeFile);

        long total = TimerHolder.stop("total");
        TimerHolder.release();
        log.info("总耗时：{} ms", total);
    }

    public static List<OpticsRelevantObject> orderGenerate(List<OpticsRelevantObject> oldObjects) {

        // 根据距离原点的距离排序
        // sort by distance to origin
        List<OpticsRelevantObject> relevantObjects = oldObjects
                .stream()
                .map(obj -> {
                    OpticsRelevantObject opticsRelevantObject = new OpticsRelevantObject();
                    opticsRelevantObject.setObjectId(obj.getObjectId());
                    opticsRelevantObject.setName(obj.getName());
                    opticsRelevantObject.setCoordinate(obj.getCoordinate());
                    opticsRelevantObject.setLabels(obj.getLabels());
                    return opticsRelevantObject;
                })
                .sorted(
                        Comparator.comparingDouble(obj -> CommonUtil.calculateDistance(obj.getCoordinate(), new double[]{0, 0})
                        )).collect(Collectors.toList());
        // 构建RTree
        // build RTree
        List<Entry<OpticsRelevantObject, GeoPointDouble>> entryList = relevantObjects.stream()
                .map(
                        obj -> Entries.entry(obj, GeoPointDouble.create(
                                        obj.getCoordinate()[0], obj.getCoordinate()[1]
                                )
                        )
                ).collect(Collectors.toList());

        RTree<OpticsRelevantObject, GeoPointDouble> rTree = RTree.create(entryList);

        // 结果集
        // result set
        LinkedHashSet<OpticsRelevantObject> resultQueue = new LinkedHashSet<>();

        // 从数据集中取一个对象
        // take an object from the dataset
        for (OpticsRelevantObject relevantObject : relevantObjects) {

            // 如果已经在结果集中，跳过
            // if already in the result set, skip
            if (resultQueue.contains(relevantObject)) {
                continue;
            }

            // 加入结果集
            // add to the result set
            resultQueue.add(relevantObject);

            // 范围查询结果
            // range query result
            List<OpticsRelevantObject> rangeQueryResult = new ArrayList<>();
            rTree.nearest(
                    GeoPointDouble.create(relevantObject.getCoordinate()[0], relevantObject.getCoordinate()[1]),
                    eps,
                    Integer.MAX_VALUE
            ).forEach(
                    entry -> rangeQueryResult.add(entry.value())
            );
            // 不是核心点，跳过
            // not a core point, skip
            if (rangeQueryResult.size() < minPts) {
                continue;
            }
            // 根据距离排序
            // sort by distance
            rangeQueryResult.sort(
                    Comparator.comparingDouble(
                            obj -> CommonUtil.calculateDistance(obj.getCoordinate(), relevantObject.getCoordinate())
                    )
            );
            // 计算核心距离
            // calculate core distance
            relevantObject.setCoreDistance(
                    CommonUtil.calculateDistance(
                            rangeQueryResult.get(minPts - 1).getCoordinate(),
                            relevantObject.getCoordinate()
                    )
            );
            // 准备优先队列
            // prepare priority queue
            SortedSet<OpticsRelevantObject> priorityQueue = new TreeSet<>(
                    new Comparator<OpticsRelevantObject>() {
                        @Override
                        public int compare(OpticsRelevantObject o1, OpticsRelevantObject o2) {
                            int compare = Double.compare(o1.getCoreDistance(), o2.getCoreDistance());
                            if (compare == 0) {
                                return o1.compareTo(o2);
                            }
                            return compare;
                        }
                    }
            );
            // 遍历范围查询结果
            // iterate range query result
            for (OpticsRelevantObject opticsRelevantObject : rangeQueryResult) {
                // 如果已经在结果集中，跳过
                // if already in the result set, skip
                if (resultQueue.contains(opticsRelevantObject)) {
                    continue;
                }
                // 计算新的可达距离
                // calculate new reachable distance
                opticsRelevantObject.setReachableDistance(
                        Math.max(
                                relevantObject.getCoreDistance(),
                                CommonUtil.calculateDistance(relevantObject.getCoordinate(), opticsRelevantObject.getCoordinate()
                                ))
                );
                // 将当前点加入优先队列
                // add current point to the priority queue
                priorityQueue.add(opticsRelevantObject);

            }
            // 从优先队列中获取下一个对象
            while (!priorityQueue.isEmpty()) {

                OpticsRelevantObject currentExtend = priorityQueue.first();
                priorityQueue.remove(currentExtend);
                // 如果已经在结果集中，跳过
                // if already in the result set, skip
                if (resultQueue.contains(currentExtend)) {
                    continue;
                }
                // 加入结果集
                // add to the result set
                resultQueue.add(currentExtend);
                // 范围查询结果
                // range query result
                List<OpticsRelevantObject> currentExtendRangeQueryResult = new ArrayList<>();
                rTree.nearest(
                        GeoPointDouble.create(currentExtend.getCoordinate()[0], currentExtend.getCoordinate()[1]),
                        eps,
                        Integer.MAX_VALUE
                ).forEach(
                        entry -> currentExtendRangeQueryResult.add(entry.value())
                );
                // 不是核心点，无法继续扩展，跳过
                // not a core point, cannot extend, skip
                if (currentExtendRangeQueryResult.size() < minPts) {
                    continue;
                }
                // 根据距离排序
                // sort by distance
                currentExtendRangeQueryResult.sort(
                        Comparator.comparingDouble(
                                obj -> CommonUtil.calculateDistance(obj.getCoordinate(), currentExtend.getCoordinate())
                        )
                );
                // 计算核心距离
                // calculate core distance
                currentExtend.setCoreDistance(
                        CommonUtil.calculateDistance(
                                currentExtendRangeQueryResult.get(minPts - 1).getCoordinate(),
                                currentExtend.getCoordinate()
                        )
                );

                // 遍历范围查询结果
                // iterate range query result
                for (OpticsRelevantObject opticsRelevantObject : currentExtendRangeQueryResult) {
                    // 如果已经在结果集中，跳过
                    // if already in the result set, skip
                    if (resultQueue.contains(opticsRelevantObject)) {
                        continue;
                    }
                    // 将当前对象从优先队列删除
                    // remove current object from the priority queue
                    priorityQueue.remove(opticsRelevantObject);
                    // 获取原始可达距离
                    // get original reachable distance
                    double oriDist = opticsRelevantObject.getReachableDistance();
                    // 计算新的可达距离
                    // calculate new reachable distance
                    double newDist = Math.max(
                            currentExtend.getCoreDistance(), CommonUtil.calculateDistance(currentExtend.getCoordinate(), opticsRelevantObject.getCoordinate()
                            ));
                    opticsRelevantObject.setReachableDistance(
                            Math.min(oriDist, newDist)
                    );
                    // 将当前点加入优先队列
                    priorityQueue.add(opticsRelevantObject);

                }


            }

        }

        return new ArrayList<>(resultQueue);
    }
}
