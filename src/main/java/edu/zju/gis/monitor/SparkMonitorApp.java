package edu.zju.gis.monitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan({"edu.zju.gis.monitor.mapper"})
@SpringBootApplication
public class SparkMonitorApp {

    public static void main(String[] args) {
        SpringApplication.run(SparkMonitorApp.class, args);
    }

}
