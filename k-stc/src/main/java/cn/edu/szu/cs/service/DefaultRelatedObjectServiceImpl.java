package cn.edu.szu.cs.service;

import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.entity.SimpleRelatedObject;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  gain related objects from file.
 * @author Whitence
 * @date 2023/11/2 19:13
 * @version 1.0
 */
public class DefaultRelatedObjectServiceImpl implements IRelatedObjectService{

    private Map<String, RelatedObject> map = new HashMap<>();

    public DefaultRelatedObjectServiceImpl(){

        InputStream inputStream = new ClassPathResource("objs.txt").getStream();

        IoUtil.readLines(
                new InputStreamReader(inputStream),
                new LineHandler() {
                    @Override
                    public void handle(String line) {
                        SimpleRelatedObject simpleRelatedObject = JSONObject.parseObject(line, SimpleRelatedObject.class);
                        map.put(simpleRelatedObject.getObjectId(),simpleRelatedObject);
                    }
                }
        );
    }

    @Override
    public RelatedObject getById(String id) {
        return map.get(id);
    }

    @Override
    public List<RelatedObject> getByIds(List<String> ids) {
        return ids.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<String> getLabelsById(String id) {
        return map.get(id).getLabels();
    }

    @Override
    public List<RelatedObject> getAll() {
        return new ArrayList<>(map.values());
    }
}
