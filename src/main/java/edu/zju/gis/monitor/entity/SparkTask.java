package edu.zju.gis.monitor.entity;

import lombok.Data;

/**
 * @author yanlong_lee@qq.com
 * @version 1.0 2019/04/12
 */
@Data
public class SparkTask {
    String id;
    String name;
    String mainClass;
    String jarFile;
    String state;
    String finalStatus;
    String result;
    String args;
    String driverMemory;
    String executorMemory;
    Integer numExecutors;
    Integer executorCores;
    Integer parallelism;
}
