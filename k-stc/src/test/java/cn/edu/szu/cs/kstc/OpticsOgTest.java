package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.DataFetchResult;
import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class OpticsOgTest {
    public static void main(String[] args) {

        KstcQuery query = new KstcQuery();
        query.setKeywords(Arrays.asList("restaurants", "food"));
        query.setK(10);
        query.setEpsilon(1000);
        query.setMinPts(10);
        query.setMaxDistance(Double.MAX_VALUE);
        query.setCoordinate(new double[]{-75.16,39.95});
        query.setCommand(DataFetchConstant.OPTICS_BASED_APPROACH_OG);

        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(DataFetchConstant.OPERATIONAL_LAYER,
                DataFetchConstant.OPTICS_BASED_APPROACH_OG,
                JSON.toJSONString(query));

        List<Set<DbScanRelevantObject>> result = new ArrayList<>();

        List<?> list = Convert.toList(task.getData());
        for (Object o : list) {
            result.add(Convert.toSet(DbScanRelevantObject.class, o));
        }

        System.out.println(result.size());

    }
}
