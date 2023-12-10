package com.edu.szu.config;

import com.edu.szu.KDVManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KDVConfig {
    @Bean
    public KDVManager kdvManager(){
        return new KDVManager();
    }
}
