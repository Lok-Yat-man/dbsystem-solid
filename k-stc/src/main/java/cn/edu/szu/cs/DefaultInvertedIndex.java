package cn.edu.szu.cs;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *  DefaultLeafInvertedIndex
 * @author Whitence
 * @date 2023/10/17 21:26
 * @version 1.0
 */
public class DefaultInvertedIndex implements InvertedIndex<RelatedObject>{

    private Map<String,List<String>> map;
    private IRelatedObjectService relatedObjectService;

    public DefaultInvertedIndex(IRelatedObjectService relatedObjectService){

        this.relatedObjectService = relatedObjectService;

        map = new ConcurrentHashMap<>();

        try(
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("iFile.txt").getStream()))
                ){
            String str = "";
            while ((str=reader.readLine())!=null){
                String listStr = reader.readLine();
                List<String> strings = JSONObject.parseArray(listStr, String.class);
                map.put(
                        str,
                        strings
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public PriorityQueue<RelatedObject> getSList(List<String> keywords,Coordinate coordinate,double maxDistance, Comparator<RelatedObject> comparator) {

        PriorityQueue<RelatedObject> relatedObjects = new PriorityQueue<>(comparator);
        if(CollUtil.isEmpty(keywords)){
            return relatedObjects;
        }

        String first = keywords.get(0);
        Set<String> objIds = new HashSet<>(map.getOrDefault(first,new ArrayList<>()));
        for(int i=1;i<keywords.size();i++){
            objIds.retainAll(
                    map.getOrDefault(keywords.get(i),new ArrayList<>())
            );
        }

        relatedObjects.addAll(
                objIds.stream()
                        .map(relatedObjectService::getById)
                        .filter(Objects::nonNull)
                        .filter(relatedObject -> CommonAlgorithm.calculateDistance(relatedObject.getCoordinate(),coordinate)<maxDistance)
                        .collect(Collectors.toList())
        );
        return relatedObjects;
    }

    @Override
    public List<String> keys() {
        return new ArrayList<>(map.keySet());
    }


}
