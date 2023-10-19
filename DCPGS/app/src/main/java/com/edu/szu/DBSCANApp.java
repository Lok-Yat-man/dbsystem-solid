package com.edu.szu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({
        DCPGSEndpoint.class,
        KDVEndpoint.class,
})
@SpringBootApplication
public class DBSCANApp {

    public static void main(String[] args) {
        // 获取当前类的类加载器
        ClassLoader classLoader = DBSCANApp.class.getClassLoader();

        // 获取类路径
        String classpath = System.getProperty("java.class.path");

        // 打印类路径
        System.out.println("Classpath: " + classpath);

        SpringApplication.run(DBSCANApp.class, args);

    }

}
