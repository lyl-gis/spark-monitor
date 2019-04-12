package edu.zju.gis.monitor.service.impl;

import edu.zju.gis.monitor.config.CommonSetting;
import edu.zju.gis.monitor.entity.SparkTask;
import edu.zju.gis.monitor.model.SparkTaskState;
import edu.zju.gis.monitor.service.SparkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.deploy.yarn.Client;
import org.apache.spark.deploy.yarn.ClientArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yanlong_lee@qq.com
 * @version 1.0 2019/04/12
 */
@Slf4j
@Service
public class SparkServiceImpl implements SparkService, Closeable {
    @Autowired
    CommonSetting setting;

    private YarnClient yarnClient;

    public SparkServiceImpl() {
        yarnClient = YarnClient.createYarnClient();
        yarnClient.init(new Configuration());
        yarnClient.start();
    }

    @Override
    public void close() {
        yarnClient.stop();
    }

    @Override
    public void submit(SparkTask sparkTask) {
        List<String> args = new ArrayList<>();
        Collections.addAll(args, "--jar", sparkTask.getJarFile(), "--class", sparkTask.getMainClass());
        String[] appArgs = sparkTask.getArgs().isEmpty() ? new String[0] : sparkTask.getArgs().split("");
        for (String arg : appArgs) {
            args.add("--arg");
            args.add(arg);
        }
        System.setProperty("SPARK_YARN_MODE", "true");// identify that you will be using Spark as YARN mode
        SparkConf sparkConf = new SparkConf();
        if (!sparkTask.getName().isEmpty())
            sparkConf.setAppName(sparkTask.getName());
        sparkConf.set("spark.yarn.jars", setting.getSparkYarnJars());
        // 设置为true，不删除缓存的jar包，因为现在提交yarn任务是使用的代码配置，没有配置文件，删除缓存的jar包有问题，
        sparkConf.set("spark.yarn.preserve.staging.files", "true");
        sparkConf.set("spark.sql.session.timeZone", "Asia/Shanghai");
        sparkConf.set("spark.submit.deployMode", "cluster");//可防止报错找不到driver
        sparkConf.set("spark.driver.memory", sparkTask.getDriverMemory());
        sparkConf.set("spark.executor.memory", sparkTask.getExecutorMemory());
        sparkConf.set("spark.executor.instances", "" + sparkTask.getNumExecutors());
        sparkConf.set("spark.executor.cores", "" + sparkTask.getExecutorCores());
        sparkConf.set("spark.default.parallelism", "" + sparkTask.getParallelism());
        //此处需从core-site,yarn-site等文件中读取
        ClientArguments cArgs = new ClientArguments(args.toArray(new String[0]));
        Client client = new Client(cArgs, sparkConf);
        ApplicationId appId = client.submitApplication();
        sparkTask.setId(appId.toString());
    }

    @Override
    public void kill(String appId) throws IOException, YarnException {
        yarnClient.killApplication(ConverterUtils.toApplicationId(appId));
    }

    @Override
    public SparkTaskState getState(String appId) throws IOException, YarnException {
        ApplicationId app = ConverterUtils.toApplicationId(appId);
        SparkTaskState taskState = new SparkTaskState();
        // 设置任务id
        taskState.setAppId(appId);
        ApplicationReport report = yarnClient.getApplicationReport(app);
        YarnApplicationState state = report.getYarnApplicationState();
        taskState.setState(state.name());
        float progress = report.getProgress();
        taskState.setProgress(progress);
        FinalApplicationStatus status = report.getFinalApplicationStatus();
        taskState.setFinalStatus(status.name());
        return taskState;
    }
}
