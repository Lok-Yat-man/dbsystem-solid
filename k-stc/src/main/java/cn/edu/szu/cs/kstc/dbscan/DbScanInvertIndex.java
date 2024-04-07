package cn.edu.szu.cs.kstc.dbscan;

import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.kstc.InvertedIndex;

import java.util.List;

public class DbScanInvertIndex implements InvertedIndex<DbScanRelevantObject> {




    @Override
    public List<DbScanRelevantObject> getObjectsByKeyword(String keyword) {
        return null;
    }

    @Override
    public List<DbScanRelevantObject> getObjectsByKeywords(List<String> keywords) {
        return null;
    }
}
