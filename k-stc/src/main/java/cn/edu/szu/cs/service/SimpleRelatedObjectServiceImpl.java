package cn.edu.szu.cs.service;

import cn.edu.szu.cs.entity.SimpleRelatedObject;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSON;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * gain related objects from file.
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/11/2 19:13
 */
public class SimpleRelatedObjectServiceImpl implements IRelatedObjectService<SimpleRelatedObject> {

    private final Map<String, SimpleRelatedObject> objs = new HashMap<>();

    public SimpleRelatedObjectServiceImpl() {
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
    public SimpleRelatedObject getById(String id) {
        return objs.get(id);
    }

    @Override
    public List<SimpleRelatedObject> getByIds(List<String> ids) {
        return ids.stream().filter(Objects::nonNull).map(objs::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<String> getLabelsById(String id) {
        return Optional
                .ofNullable(objs.get(id).getLabels())
                .orElse(new ArrayList<>());
    }

    @Override
    public List<SimpleRelatedObject> getAll() {
        return new ArrayList<>(objs.values());
    }

}
