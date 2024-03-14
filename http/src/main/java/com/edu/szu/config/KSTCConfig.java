package com.edu.szu.config;

import cn.edu.szu.cs.entity.DefaultRelatedObject;
import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.ds.irtree.IRTree;
import cn.edu.szu.cs.ds.irtree.SimpleIRTree;
import cn.edu.szu.cs.ivtidx.DefaultInvertedIndex;
import cn.edu.szu.cs.ivtidx.InvertedIndex;
import cn.edu.szu.cs.kstc.KSTC;
import cn.edu.szu.cs.kstc.SimpleKSTC;
import cn.edu.szu.cs.kstc.SimpleKSTC2;
import cn.edu.szu.cs.kstc.SimpleKSTC3;
import cn.edu.szu.cs.service.DefaultRelatedObjectServiceImpl;
import cn.edu.szu.cs.service.IRelatedObjectService;
import cn.hutool.core.io.resource.ClassPathResource;
import com.edu.szu.service.KstcService;
import com.edu.szu.service.impl.KstcServiceImpl;
import com.github.davidmoten.rtree.InternalStructure;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Geometry;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class KSTCConfig {

    /**
     * relatedObjectService
     * @return
     */
    @Bean
    public IRelatedObjectService relatedObjectService(){
        return new DefaultRelatedObjectServiceImpl();
    }

    /**
     * rTree
     * @return
     */
    @SneakyThrows
    @Bean
    public RTree<String, Geometry> rTree(){
        InputStream inputStream = new ClassPathResource("rtree.txt").getStream();
        int available = inputStream.available();
        return Serializers.flatBuffers().utf8().read(inputStream, available, InternalStructure.DEFAULT);
    }

    /**
     *
     * @param rTree
     * @param relatedObjectService
     * @return
     */
    @Bean
    public IRTree<RelatedObject> irTree(RTree<String,Geometry> rTree, IRelatedObjectService relatedObjectService){
        return new SimpleIRTree(rTree,relatedObjectService);
    }

    /**
     *
     * @param relatedObjectService
     * @return
     */
    @Bean
    public InvertedIndex<DefaultRelatedObject> invertedIndex(IRelatedObjectService relatedObjectService){
        return new DefaultInvertedIndex(relatedObjectService);
    }
    //
    ///**
    // * kstc alg
    // * @return
    // */
    //@Bean("kstc")
    //public KSTC<RelatedObject> kstc(IRTree<RelatedObject> irTree, InvertedIndex<RelatedObject> invertedIndex){
    //    return new SimpleKSTC<>(irTree,invertedIndex);
    //}
    //
    ///**
    // * kstc alg
    // * @return
    // */
    //@Bean("kstc2")
    //public KSTC<RelatedObject> kstc2(IRelatedObjectService relatedObjectService){
    //    return new SimpleKSTC2<>(relatedObjectService);
    //}
    //
    //
    ///**
    // * kstc alg
    // * @return
    // */
    //@Bean("kstc3")
    //public KSTC<RelatedObject> kstc3(IRelatedObjectService relatedObjectService){
    //    return new SimpleKSTC3<>(relatedObjectService);
    //}
    //
    //@Bean
    //public KstcService kstcService(@Qualifier("kstc2") @Autowired KSTC<RelatedObject> kstc){
    //    return new KstcServiceImpl(kstc);
    //}

}
