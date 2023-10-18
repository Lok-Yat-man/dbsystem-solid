package com.edu.szu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({
        DCPGSEndpoint.class
})
@SpringBootApplication
public class DBSCANApp {

    public static void main(String[] args) {
        SpringApplication.run(DBSCANApp.class, args);
    }

}
