package cn.edu.szu.cs.kstc.dbscan;

import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.infrastructure.IRelevantObjectDataLoader;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSON;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DbScanDataLoaderImpl
 *
 * @author Whitence
 * @version 1.0
 * @date 2024/4/5 20:17
 */
public class DbScanDataObjectDataLoaderImpl implements IRelevantObjectDataLoader<DbScanRelevantObject> {
    private final Map<String, DbScanRelevantObject> objs = new HashMap<>();

    public DbScanDataObjectDataLoaderImpl() {
        InputStream inputStream = new ClassPathResource("objs.txt").getStream();

        IoUtil.readLines(
                new InputStreamReader(inputStream),
                new LineHandler() {
                    @Override
                    public void handle(String line) {
                        DbScanRelevantObject defaultRelatedObject = JSON.parseObject(line, DbScanRelevantObject.class);
                        objs.put(defaultRelatedObject.getObjectId(), defaultRelatedObject);
                    }
                }
        );


    }

    @Override
    public DbScanRelevantObject getById(String id) {
        return objs.get(id);
    }

    @Override
    public List<DbScanRelevantObject> getByIds(List<String> ids) {
        return ids.stream()
                .filter(Objects::nonNull)
                .map(objs::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<DbScanRelevantObject> getAll() {
        return new ArrayList<>(objs.values());
    }

    @Override
    public List<DbScanRelevantObject> getObjectsByKeyword(String keyword) {
        return null;
    }

    @Override
    public List<DbScanRelevantObject> getObjectsByKeywords(List<String> keywords) {
        return null;
    }

}
