package com.edu.szu;

import com.edu.szu.config.DCPGSConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({
        DCPGSConfig.class,
        DCPGSEndpoint.class,
        KSTCEndpoint.class,
        KDVEndpoint.class,
})
@SpringBootApplication
public class DBSCANApp {

    public static void main(String[] args) {
        SpringApplication.run(DBSCANApp.class, args);
    }

}
