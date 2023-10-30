package com.edu.szu.config;

import cn.edu.szu.cs.KSTC;
import cn.edu.szu.cs.SimpleKSTC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KSTCConfig {


    /**
     * kstc alg
     * @return
     */
    @Bean
    public KSTC kstc(){
        return new SimpleKSTC();
    }

}
