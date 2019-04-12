package edu.zju.gis.monitor.config;

import lombok.Data;

/**
 * @author yanlong_lee@qq.com
 * @version 1.0 2019/04/12
 */
@Data
public class ResourceConfig {
    String driverMemory;
    String executorMemory;
    int numExecutors;
    int executorCores;
    int parallelism;
}