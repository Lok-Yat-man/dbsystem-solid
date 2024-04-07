package com.edu.szu.config;

import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;
import com.edu.szu.service.KstcService;
import com.edu.szu.service.impl.KstcServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class KSTCConfig {


    /**
     * default alg
     * @return
     */
    @Bean("defaultKstc")
    public TopKSpatialTextualClustersRetrieval<DefaultRelevantObject> kstc(KSTCFactory<DefaultRelevantObject> kstcFactory){
        return kstcFactory.createTopKSpatialTextualClustersRetrieval();
    }

    @Bean("defaultKstcFactory")
    KSTCFactory<DefaultRelevantObject> defaultKstcFactory(){
        return new DefaultDBScanBasedApproachFactory();
    }

    @Bean("DefaultKstcServiceImpl")
    public KstcService<DefaultRelevantObject> defaultKstcService(@Autowired @Qualifier("defaultKstc") TopKSpatialTextualClustersRetrieval<DefaultRelevantObject> kstc){
        return new KstcServiceImpl<>(kstc);
    }




    /**
     * simple alg
     * @return
     */
    @Bean("simpleKstcFactory")
    KSTCFactory<SimpleRelevantObject> simpleKstcFactory(){
        return new SimpleDBScanBasedApproachFactory();
    }

    @Bean("simpleKstc")
    public TopKSpatialTextualClustersRetrieval<SimpleRelevantObject> simpleKstc(KSTCFactory<SimpleRelevantObject> kstcFactory){
        return kstcFactory.createTopKSpatialTextualClustersRetrieval();
    }

    @Bean("SimpleKstcServiceImpl")
    public KstcService<SimpleRelevantObject> simpleKstcService(@Autowired @Qualifier("simpleKstc") TopKSpatialTextualClustersRetrieval<SimpleRelevantObject> kstc){
        return new KstcServiceImpl<>(kstc);
    }
}
