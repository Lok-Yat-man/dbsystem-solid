package cn.edu.szu.cs.ivtidx;


import cn.edu.szu.cs.entity.Coordinate;
import cn.edu.szu.cs.service.IRelatedObjectService;
import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.util.CommonAlgorithm;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  DefaultLeafInvertedIndex
 * @author Whitence
 * @date 2023/10/17 21:26
 * @version 1.0
 */
public class TrieInvertedIndex implements InvertedIndex<RelatedObject> {

    private static volatile SoftReference<LRUCache<String,List<String>>> cacheSoftReference = null;
    private static final long DEFAULT_TIMEOUT = 30_000L;
    private LRUCache<String,List<String>> getCache(){
        if(cacheSoftReference == null){
            synchronized (TrieInvertedIndex.class){
                if(cacheSoftReference == null){
                    cacheSoftReference=new SoftReference<>(CacheUtil.newLRUCache(32,DEFAULT_TIMEOUT));
                }
            }
        }
        return cacheSoftReference.get();
    }

    private IRelatedObjectService relatedObjectService;
    private Trie root;

    private static class Trie{
        boolean isWord;
        Map<Character,Trie> children;
        List<String> objIds;
    }

    private void put(String keyword,List<String> objIds){
        Trie cur=root;
        for (char ch : keyword.toCharArray()) {
            if(root.children == null){
                root.children = new HashMap<>();
            }
            root.children.putIfAbsent(ch,new Trie());
            cur=root.children.get(ch);
        }
        cur.isWord=true;
        if(cur.objIds == null){
            cur.objIds=new LinkedList<>();
        }
        cur.objIds.addAll(objIds);
    }


    private List<String> search(String keyword){


    return null;
    }


    public TrieInvertedIndex(IRelatedObjectService relatedObjectService){
        this.relatedObjectService = relatedObjectService;
        this.root=new Trie();
        try(
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("iFile.txt").getStream()))
                ){
            String str = "";
            while ((str=reader.readLine())!=null){
                String listStr = reader.readLine();
                List<String> strings = JSONObject.parseArray(listStr, String.class);



            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized PriorityQueue<RelatedObject> getSList(List<String> keywords, Coordinate coordinate, double maxDistance, Comparator<RelatedObject> comparator) {

        return null;
    }

    @Override
    public Map<String, List<String>> getAll() {
        return null;
    }

}
