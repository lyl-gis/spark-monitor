package edu.zju.gis.monitor.service;

import edu.zju.gis.monitor.entity.SparkTask;
import edu.zju.gis.monitor.model.SparkTaskState;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.IOException;

/**
 * @author yanlong_lee@qq.com
 * @version 1.0 2019/04/12
 */
public interface SparkService {
    void submit(SparkTask sparkTask);

    void kill(String appId) throws IOException, YarnException;

    SparkTaskState getState(String appId) throws IOException, YarnException;
}
