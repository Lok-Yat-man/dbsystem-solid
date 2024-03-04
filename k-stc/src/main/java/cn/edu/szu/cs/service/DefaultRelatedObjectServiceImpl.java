package cn.edu.szu.cs.service;

import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.entity.SimpleRelatedObject;
import cn.edu.szu.cs.strategy.WeightCalculationStrategy;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSON;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * gain related objects from file.
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/11/2 19:13
 */
public class DefaultRelatedObjectServiceImpl implements IRelatedObjectService {

    private final Map<String, RelatedObject> objs = new ConcurrentHashMap<>();

    public DefaultRelatedObjectServiceImpl() {
        InputStream inputStream = new ClassPathResource("objs.txt").getStream();

        IoUtil.readLines(
                new InputStreamReader(inputStream),
                new LineHandler() {
                    @Override
                    public void handle(String line) {
                        SimpleRelatedObject simpleRelatedObject = JSON.parseObject(line, SimpleRelatedObject.class);
                        objs.put(simpleRelatedObject.getObjectId(), simpleRelatedObject);
                    }
                }
        );


    }

    @Override
    public RelatedObject getById(String id) {
        return objs.get(id);
    }

    @Override
    public List<RelatedObject> getByIds(List<String> ids) {
        return ids.stream().filter(Objects::nonNull).map(objs::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<String> getLabelsById(String id) {
        return objs.get(id).getLabels();
    }

    @Override
    public List<RelatedObject> getAll() {
        return new ArrayList<>(objs.values());
    }

}
