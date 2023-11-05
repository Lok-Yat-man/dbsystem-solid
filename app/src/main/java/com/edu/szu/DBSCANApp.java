package com.edu.szu;

import com.edu.szu.config.DCPGSConfig;
import com.edu.szu.config.KSTCConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@Import({
        // config
        DCPGSConfig.class,
        KSTCConfig.class,

        // endpoint
        DCPGSEndpoint.class,
        KSTCEndpoint.class,
        KDVEndpoint.class,
})
@SpringBootApplication
@Slf4j
public class DBSCANApp {

    public static void main(String[] args) {
        SpringApplication.run(DBSCANApp.class, args);
    }

}
