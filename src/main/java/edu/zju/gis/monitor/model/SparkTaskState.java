package edu.zju.gis.monitor.model;

import lombok.Data;

/**
 * @author yanlong_lee@qq.com
 * @version 1.0 2019/04/12
 */
@Data
public class SparkTaskState {
    String appId;
    String state;
    float progress;
    String finalStatus;
}
