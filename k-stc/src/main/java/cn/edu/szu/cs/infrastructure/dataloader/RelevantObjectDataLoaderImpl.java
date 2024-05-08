package cn.edu.szu.cs.infrastructure.dataloader;

import cn.edu.szu.cs.kstc.RelevantObject;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据层实现类，该类用于从输入流中加载数据对象。
 * <p>  data loader implementation class, this class is used to load data objects from input stream.
 *
 * @author Whitence
 * @version 1.0
 * @date 2024/4/5 20:17
 */
public class RelevantObjectDataLoaderImpl<T extends RelevantObject> implements IRelevantObjectDataLoader<T> {
    /**
     * 数据对象集合. 存储id和对象数据的映射
     * <p> Data object collection. Store the mapping of id and object data
     */
    private final Map<String, T> objs = new ConcurrentHashMap<>();

    /**
     * 标签-对象id映射
     * <p> Label-object id mapping
     */
    private final Map<String, Set<T>> labelObjectMap = new ConcurrentHashMap<>();

    private static Log log = LogFactory.get();

    public RelevantObjectDataLoaderImpl(InputStream inputStream, Class<T> clazz) {

        log.info("Loading data from input stream...");
        IoUtil.readLines(
                new InputStreamReader(inputStream),
                new LineHandler() {
                    @Override
                    public void handle(String line) {
                        T object = JSON.parseObject(line, clazz);
                        objs.put(object.getObjectId(), object);
                    }
                }
        );

        List<T> relevantObjects = new ArrayList<>(objs.values());

        // 构建标签-对象id映射
        if (CollUtil.isEmpty(relevantObjects)) {
            throw new IllegalArgumentException("relevantObjects is empty");
        }

        for (T relatedObject : relevantObjects) {
            List<String> labels = relatedObject.getLabels();
            if (CollUtil.isNotEmpty(labels)) {
                for (String label : labels) {
                    labelObjectMap.putIfAbsent(label, new HashSet<>());
                    labelObjectMap.get(label).add(relatedObject);
                }
            }
        }

        log.info("Data loaded successfully.object size:{},label size:{}", objs.size(), labelObjectMap.size());


    }

    /**
     * 通过id获取对象
     * <p> Get object by id
     *
     * @param id
     * @return
     */
    @Override
    public T getById(String id) {
        return objs.get(id);
    }

    /**
     * 通过id列表获取对象
     * <p> Get object by id list
     *
     * @param ids
     * @return
     */
    @Override
    public synchronized List<T> getByIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .map(objs::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 通过id获取对象的标签
     * <p> Get object's labels by id
     *
     * @return
     */
    @Override
    public List<T> getAll() {
        return new ArrayList<>(objs.values());
    }

    /**
     * 通过id获取对象的标签
     * <p> Get object's labels by id
     *
     * @param keyword
     * @return
     */
    @Override
    public synchronized List<T> getObjectsByKeyword(String keyword) {

        Set<T> set = labelObjectMap.get(keyword);
        if (set == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(set);
    }

    /**
     * 通过关键字列表获取对象
     * <p> Get object by keyword list
     *
     * @param keywords
     * @return
     */
    @Override
    public synchronized List<T> getObjectsByKeywords(List<String> keywords) {
        log.info("getObjectsByKeywords:{}", keywords);
        if (CollUtil.isEmpty(keywords)) {
            return Collections.emptyList();
        }

        Set<T> set = keywords.stream()
                .map(labelObjectMap::get)
                .filter(CollUtil::isNotEmpty)
                .reduce(new HashSet<>(),(set1, set2) -> {
                    set1.addAll(set2);
                    return set1;
                });

        return new ArrayList<>(set);

    }

    @Override
    public List<String> getAllLabels() {
        return new ArrayList<>(labelObjectMap.keySet());
    }

    /**
     * 通过关键字列表获取对象id,并取交集
     *
     * @param keywords
     * @return
     */
    private synchronized Set<T> getObjectIdsByKeyword(List<String> keywords) {

        return keywords.stream()
                .map(labelObjectMap::get)
                .filter(CollUtil::isNotEmpty)
                .reduce((set1, set2) -> {
                    set1.retainAll(set2);
                    return set1;
                }).orElse(new HashSet<>());
    }

    public synchronized List<String> prefixSearch(String keyword) {
        return labelObjectMap.keySet().stream()
                .filter(label -> label.startsWith(keyword))
                .collect(Collectors.toList());
    }

}
