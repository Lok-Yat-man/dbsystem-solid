package com.edu.szu;

import com.edu.szu.config.*;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({
        // config
        DCPGSConfig.class,
        KSTCConfig.class,
        KDVConfig.class,
        BSTDConfig.class,

        // endpoint
        DCPGSEndpoint.class,
        KSTCEndpoint.class,
        KDVEndpoint.class,
        BSTDEndPoint.class,
        topkMain.class
})
@SpringBootApplication
@Log4j2
public class DBSystemApp {

    public static void main(String[] args) {
        SpringApplication.run(DBSystemApp.class, args);
    }
}
