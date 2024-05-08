package cn.edu.szu.cs.kstc.optics;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.dto.WordOrderingIndexQueryDTO;
import cn.edu.szu.cs.entity.DataFetchResult;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.entity.OpticsRelevantObject;
import cn.edu.szu.cs.kstc.Context;
import cn.edu.szu.cs.util.CommonUtil;
import cn.edu.szu.cs.util.TimerHolder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.stream.Collectors;

/**
 *  OpticsOm
 * @author Whitence
 * @date 2024/4/5 11:14
 * @version 1.0
 */
public class OpticsOm extends AbstractOpticsBasedApproach<OpticsRelevantObject>{

    private Log log = LogFactory.get();

    public OpticsOm() {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    private static class OpticsOmContext extends Context<OpticsRelevantObject> {


    }

    @Override
    protected Context<OpticsRelevantObject> initContext(KstcQuery query) {
        OpticsOmContext context = new OpticsOmContext();
        context.setQuery(query);

        return context;
    }

    @Override
    protected void beforeOpticsBasedApproach(Context<OpticsRelevantObject> context) {

        TimerHolder.start("OpticsOm");

    }

    @Override
    protected List<OpticsRelevantObject> generateResultQueue(Context<OpticsRelevantObject> context) {

        OpticsOmContext opticsOmContext = (OpticsOmContext) context;
        KstcQuery query = opticsOmContext.getQuery();

        if(query.getKeywords().size() == 1){
            return new ArrayList<>(getOrderingList(query.getKeywords().get(0)));
        }

        List<Queue<OpticsRelevantObject>> queueList = query.getKeywords()
                .stream()
                .map(this::getOrderingList)
                .filter(CollUtil::isNotEmpty)
                .collect(Collectors.toList());

        List<OpticsRelevantObject> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        PriorityQueue<Tuple> priorityQueue = new PriorityQueue<>(
                new Comparator<Tuple>() {
                    @Override
                    public int compare(Tuple o1, Tuple o2) {
                        OpticsRelevantObject a = o1.get(0);
                        OpticsRelevantObject b = o2.get(0);
                        int compare = Double.compare(a.getReachableDistance(), b.getReachableDistance());
                        if(compare == 0){
                            return a.compareTo(b);
                        }
                        return compare;
                    }
                }
        );

        for (Queue<OpticsRelevantObject> queue : queueList) {
            OpticsRelevantObject relevantObject = queue.poll();
            priorityQueue.add(new Tuple(relevantObject,queue));
        }

        while(!priorityQueue.isEmpty()){

            Tuple tuple = priorityQueue.poll();
            OpticsRelevantObject opticsRelevantObject = tuple.get(0);
            result.add(opticsRelevantObject);
            visited.add(opticsRelevantObject.getObjectId());

            Queue<OpticsRelevantObject> opticsRelevantObjects = tuple.get(1);

            while (!opticsRelevantObjects.isEmpty() && visited.contains(opticsRelevantObjects.peek().getObjectId())){
                opticsRelevantObjects.poll();
            }

            if(opticsRelevantObjects.isEmpty()){
                continue;
            }

            OpticsRelevantObject nextObj = opticsRelevantObjects.poll();

            priorityQueue.add(new Tuple(nextObj,opticsRelevantObjects));
        }
        return result.stream()
                .filter(obj -> CommonUtil.calculateDistance(obj.getCoordinate(), query.getCoordinate()) <= query.getMaxDistance())
                .collect(Collectors.toList());
    }

    private Queue<OpticsRelevantObject> getOrderingList(String keyword){

        WordOrderingIndexQueryDTO wordOrderingIndexQueryDTO = new WordOrderingIndexQueryDTO();
        wordOrderingIndexQueryDTO.setKeyword(keyword);
        wordOrderingIndexQueryDTO.setCommand(DataFetchConstant.WORD_ORDERING_INDEX);

        DataFetchResult dataFetchResult = KstcDataFetchManager.generateTaskAndGet(
                DataFetchConstant.INFRASTRUCTURE_LAYER,
                DataFetchConstant.WORD_ORDERING_INDEX,
                JSON.toJSONString(wordOrderingIndexQueryDTO)
        );
        if(!dataFetchResult.isSuccess()){
            log.error("Get ordering list failed, keyword: {}", keyword);
            throw new RuntimeException("Get ordering list failed");
        }
        return (Queue<OpticsRelevantObject>) dataFetchResult.getData();
    }

    @Override
    protected void afterOpticsBasedApproach(Context<OpticsRelevantObject> context) {
        long opticsOm = TimerHolder.stop("OpticsOm");
        log.info("OpticsOm query: {}, Time Cost: {} ms", JSON.toJSONString(context.getQuery()), opticsOm);
    }

    @Override
    protected void afterGenerateResultQueue(Context<OpticsRelevantObject> context) {
        long opticsOm = TimerHolder.stop("OpticsOm");
        log.info("afterGenerateResultQueue Time Cost: {} ms", opticsOm);
    }
}
