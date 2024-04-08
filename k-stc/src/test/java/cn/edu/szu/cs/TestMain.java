package cn.edu.szu.cs;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.common.DataFetchCommandConstant;
import cn.edu.szu.cs.entity.DataFetchTask;
import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class TestMain {
    public static void main(String[] args) {


        KstcQuery query = new KstcQuery();
        query.setKeywords(Arrays.asList("food"));
        query.setK(10);
        query.setEpsilon(100);
        query.setMinPts(10);
        query.setMaxDistance(Double.MAX_VALUE);
        query.setCoordinate(new double[]{-75.16,39.95});
        query.setCommand(DataFetchCommandConstant.SIMPLE_DBSCAN_BASED_APPROACH);

        String actionId = KstcDataFetchManager.generateTask(
                DataFetchCommandConstant.SIMPLE_DBSCAN_BASED_APPROACH,
                JSON.toJSONString(query)
        );

        DataFetchTask task = KstcDataFetchManager.getTask(actionId);

        List<Set<DbScanRelevantObject>> result = new ArrayList<>();

        List<?> list = Convert.toList(task.getData());
        for (Object o : list) {
            result.add(Convert.toSet(DbScanRelevantObject.class, o));
        }

        System.out.println(result.size());

    }
}
