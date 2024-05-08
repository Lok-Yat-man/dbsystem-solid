package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.DataFetchResult;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.kstc.dbscan.SimpleDbScanBasedApproach;
import cn.edu.szu.cs.util.TimerHolder;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;


public class DbScanTest {

    private static Log log = LogFactory.get();

    @Test
    public void test1(){


        List<String> keywords = Arrays.asList("aa","local","shopping", "food", "services", "restaurants");
        SimpleDbScanBasedApproach.prepareAdvance = false;
        String resStr = "";



        for (String keyword : keywords) {

            KstcQuery query = KstcQuery.builder()
                    .command(DataFetchConstant.SIMPLE_DBSCAN_BASED_APPROACH)
                    .coordinate(new double[]{0, 0})
                    .epsilon(1000)
                    .minPts(10)
                    .keywords(Arrays.asList(keyword))
                    .k(20)
                    .maxDistance(Double.MAX_VALUE)
                    .build();

            TimerHolder.start(keyword);
            DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(
                    DataFetchConstant.OPERATIONAL_LAYER,
                    query.getCommand(), JSON.toJSONString(query));
            long stop = TimerHolder.stop(keyword);

            log.info("time||keywords: {} time cost {} ms", keyword,stop);
            resStr += keyword + ":" + stop + "ms\n";

        }

        log.info(resStr);


        KstcQuery query = KstcQuery.builder()
                .command(DataFetchConstant.SIMPLE_DBSCAN_BASED_APPROACH)
                .coordinate(new double[]{0, 0})
                .epsilon(1000)
                .minPts(10)
                .keywords(Arrays.asList("local", "shopping", "food", "services", "restaurants"))
                .k(20)
                .maxDistance(Double.MAX_VALUE)
                .build();

        TimerHolder.start("all");
        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(
                DataFetchConstant.OPERATIONAL_LAYER,
                query.getCommand(), JSON.toJSONString(query));
        long stop = TimerHolder.stop("all");

        log.info("all keywords time cost {} ms",stop);



    }


    @Test
    public void test2(){

        List<String> keywords = Arrays.asList("aa","local", "shopping", "food", "services", "restaurants");
        SimpleDbScanBasedApproach.prepareAdvance = true;
        String resStr = "";
        for (String keyword : keywords) {

            KstcQuery query = KstcQuery.builder()
                    .command(DataFetchConstant.SIMPLE_DBSCAN_BASED_APPROACH)
                    .coordinate(new double[]{0, 0})
                    .epsilon(1000)
                    .minPts(10)
                    .keywords(Arrays.asList(keyword))
                    .k(20)
                    .maxDistance(Double.MAX_VALUE)
                    .build();

            TimerHolder.start(keyword);
            DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(
                    DataFetchConstant.OPERATIONAL_LAYER,
                    query.getCommand(), JSON.toJSONString(query));
            long stop = TimerHolder.stop(keyword);

            log.info("keywords: {} time cost {} ms", keyword,stop);
            resStr += keyword + ":" + stop + "ms\n";
        }
        log.info(resStr);


        KstcQuery query = KstcQuery.builder()
                .command(DataFetchConstant.SIMPLE_DBSCAN_BASED_APPROACH)
                .coordinate(new double[]{0, 0})
                .epsilon(1000)
                .minPts(10)
                .keywords(Arrays.asList("local", "shopping", "food", "services", "restaurants"))
                .k(20)
                .maxDistance(Double.MAX_VALUE)
                .build();

        TimerHolder.start("all");
        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(
                DataFetchConstant.OPERATIONAL_LAYER,
                query.getCommand(), JSON.toJSONString(query));
        long stop = TimerHolder.stop("all");

        log.info("all keywords time cost {} ms",stop);


    }

    @Test
    public void test3(){

        KstcQuery query = KstcQuery.builder()
                .command(DataFetchConstant.SIMPLE_DBSCAN_BASED_APPROACH)
                .coordinate(new double[]{0, 0})
                .epsilon(1000)
                .minPts(10)
                .keywords(Arrays.asList("food","restaurants"))
                .k(20)
                .maxDistance(Double.MAX_VALUE)
                .build();

        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(
                DataFetchConstant.OPERATIONAL_LAYER,
                query.getCommand(), JSON.toJSONString(query));


    }

}
