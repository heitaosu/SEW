package com.company.project;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        Application.removeLocalFile();
    }

    public static void removeLocalFile() {
        try {
            System.out.println("程序启动时清理本地临时生成的制作铭牌信息的文件 start");
            File file = new File("D:\\orderrecord\\");
            FileUtils.deleteDirectory(file);
            System.out.println("程序启动时清理本地临时生成的制作铭牌信息的文件 end");
        }catch (Exception e){
            System.out.println("Application removeLocalFile Exception" + e.getMessage());
        }
    }
}