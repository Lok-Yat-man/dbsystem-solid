package cn.edu.szu.cs.kstc.optics;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.dto.WordOrderingIndexQueryDTO;
import cn.edu.szu.cs.entity.DataFetchResult;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.entity.OpticsRelevantObject;
import cn.edu.szu.cs.kstc.Context;
import cn.edu.szu.cs.util.TimerHolder;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
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

    private LRUCache<String, List<OpticsRelevantObject>> cache = new LRUCache<>(128);


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
            return getOrderingList(query.getKeywords().get(0));
        }
        List<Set<OpticsRelevantObject>> linkedHashSets = query.getKeywords()
                .stream()
                .map(keyword -> new LinkedHashSet<>(getOrderingList(keyword)))
                .filter(CollUtil::isNotEmpty)
                .collect(Collectors.toList());

        Set<OpticsRelevantObject> result = new LinkedHashSet<>();

        SortedSet<OpticsRelevantObject> priorityQueue = new TreeSet<>(
                new Comparator<OpticsRelevantObject>() {
                    @Override
                    public int compare(OpticsRelevantObject o1, OpticsRelevantObject o2) {
                        int compare = Double.compare(o1.getReachableDistance(), o2.getReachableDistance());
                        if(compare == 0){
                            return o1.compareTo(o2);
                        }
                        return compare;
                    }
                }
        );

        Map<OpticsRelevantObject,Set<OpticsRelevantObject>> map = new HashMap<>();
        for (Set<OpticsRelevantObject> linkedHashSet : linkedHashSets) {
            OpticsRelevantObject next = linkedHashSet.iterator().next();
            priorityQueue.add(next);
            map.put(next,linkedHashSet);
            linkedHashSet.remove(next);
        }


        while(CollUtil.isNotEmpty(priorityQueue)){

            OpticsRelevantObject relevantObject = priorityQueue.first();
            priorityQueue.remove(relevantObject);

            result.add(relevantObject);

            Set<OpticsRelevantObject> opticsRelevantObjects = map.get(relevantObject);
            Iterator<OpticsRelevantObject> iterator = opticsRelevantObjects.iterator();
            while (iterator.hasNext()){
                OpticsRelevantObject next = iterator.next();

                if(result.contains(next)){
                    continue;
                }
                priorityQueue.add(next);
                map.put(next,opticsRelevantObjects);
                break;
            }

        }
        return new ArrayList<>(result);
    }

    private List<OpticsRelevantObject> getOrderingList(String keyword){

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
        return Convert.toList(OpticsRelevantObject.class, dataFetchResult.getData());
    }

    @Override
    protected void afterOpticsBasedApproach(Context<OpticsRelevantObject> context) {
        long opticsOm = TimerHolder.stop("OpticsOm");
        log.info("OpticsOm query: {}, Time Cost: {} ms", JSON.toJSONString(context.getQuery()), opticsOm);
    }
}
