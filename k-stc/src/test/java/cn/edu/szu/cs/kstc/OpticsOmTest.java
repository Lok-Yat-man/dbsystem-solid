package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.DataFetchResult;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.util.TimerHolder;
import com.alibaba.fastjson.JSON;

import java.util.Arrays;
import java.util.List;


public class OpticsOmTest {
    public static void main(String[] args) {

        KstcQuery query = new KstcQuery();
        query.setKeywords(Arrays.asList("food","restaurants"));
        query.setK(200);
        query.setEpsilon(1000);
        query.setMinPts(10);
        query.setMaxDistance(Double.MAX_VALUE);
        query.setCoordinate(new double[]{0,0});
        query.setCommand(DataFetchConstant.OPTICS_BASED_APPROACH_OM);


        TimerHolder.start("test");
        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(DataFetchConstant.OPERATIONAL_LAYER,
                DataFetchConstant.OPTICS_BASED_APPROACH_OM,
                JSON.toJSONString(query));
        long test = TimerHolder.stop("test");
        System.out.println("time cost " + test + " ms");

        List data = (List) task.getData();

        System.out.println(data.size());
    }
}
