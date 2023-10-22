package cn.edu.szu.cs;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *  DefaultRelevantObjectServiceImpl
 * @author Whitence
 * @date 2023/10/17 21:36
 * @version 1.0
 */
public class DefaultRelevantObjectServiceImpl implements IRelevantObjectService {

    private Map<String,RelevantObject> relevantObjectMap = new HashMap<>();

    public DefaultRelevantObjectServiceImpl(){

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("objs.txt");

        if(resourceAsStream == null){
            throw new RuntimeException("objs.txt不存在！");
        }

        try(
                BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))
        ) {

            String str = "";
            while((str=reader.readLine())!=null){

                RelevantObject relevantObject = JSON.parseObject(str, RelevantObject.class);

                relevantObjectMap.put(relevantObject.getObjectId(), relevantObject);

            }
        }catch (Exception exception){
            exception.printStackTrace();
        }

    }

    @Override
    public RelevantObject getById(String objId) {
        return relevantObjectMap.get(objId);
    }

    @Override
    public Map<String, Double> getWeightsById(String objId) {
        return Optional.ofNullable(
                relevantObjectMap.get(objId)
        ).map(RelevantObject::getWeights)
                .orElse(new HashMap<>(1));
    }

    @Override
    public List<RelevantObject> getByIds(List<String> objIds) {
        return Optional.ofNullable(objIds)
                .map(
                        oIds->oIds
                                .stream()
                                .map(this::getById)
                                .collect(Collectors.toList())
                ).orElse(new ArrayList<>());
    }

    @Override
    public List<RelevantObject> getByIds(List<String> objIds, Predicate<RelevantObject> predicate) {
        return this.getByIds(objIds)
                .stream()
                .filter(Objects::nonNull)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public List<RelevantObject> getAll() {
        return new ArrayList<>(relevantObjectMap.values());
    }



}
