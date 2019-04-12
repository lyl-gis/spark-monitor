package edu.zju.gis.monitor.controller;

import edu.zju.gis.monitor.config.CommonSetting;
import edu.zju.gis.monitor.entity.SparkTask;
import edu.zju.gis.monitor.model.Result;
import edu.zju.gis.monitor.model.SparkTaskState;
import edu.zju.gis.monitor.service.SparkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author yanlong_lee@qq.com
 * @version 1.0 2019/04/08
 */
@Slf4j
@RestController
@RequestMapping("/spark")
public class SparkController {

    @Autowired
    CommonSetting setting;
    private ConcurrentMap<String, SparkAppHandle> handles = new ConcurrentHashMap<>();
    @Autowired
    SparkService sparkService;

    @PostMapping("/launcher/submit")
    public Result<String> submit(@RequestBody String requestBody) {
        JSONObject json = new JSONObject(requestBody);
        String appResource = json.optString("appResource", setting.getJarFile());
        String mainClass = json.getString("mainClass");
        String args = json.optString("args");
        String driverMemory = json.optString("driverMemory", "4g");
        String executorMemory = json.optString("executorMemory", "4g");
        String numExecutors = json.optString("numExecutors", "8");
        String executorCores = json.optString("executorCores", "1");
        Map<String, String> env = new HashMap<>();
        env.put("HADOOP_HOME", setting.getHadoopHome());
        SparkLauncher launcher = new SparkLauncher(env)
                .setSparkHome(setting.getSparkHome())
                .setMaster("yarn")
                .setDeployMode("cluster")
                .setAppResource(appResource)
                .setConf(SparkLauncher.DRIVER_MEMORY, driverMemory)
                .setConf(SparkLauncher.EXECUTOR_MEMORY, executorMemory)
                .setConf("spark.executor.instances", numExecutors)
                .setConf(SparkLauncher.EXECUTOR_CORES, executorCores)
                .setMainClass(mainClass);
        if (!args.isEmpty())
            launcher.addAppArgs(args.split(","));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        SparkAppHandle.Listener listener = new SparkAppHandle.Listener() {
            @Override
            public void stateChanged(SparkAppHandle handle) {
                if (handle.getAppId() != null) {
                    handles.putIfAbsent(handle.getAppId(), handle);
                    countDownLatch.countDown();
                }
                SparkAppHandle.State state = handle.getState();
                System.out.printf("app %s's state is %s\n", handle.getAppId(), state.toString());
                if (state.isFinal()) {
                    handles.remove(handle.getAppId());
                }
            }

            @Override
            public void infoChanged(SparkAppHandle handle) {
                SparkAppHandle.State state = handle.getState();
                System.out.printf("app %s changed with state being %s\n", handle.getAppId(), state.toString());
            }
        };
        Result<String> result = new Result<>();
        try {
            SparkAppHandle handle = launcher.startApplication(listener);//handle状态改变时才会产生appId
            countDownLatch.await();
            result.status("ok").data(handle.getAppId());
        } catch (IOException | InterruptedException e) {
            log.error("模型任务提交失败", e);
            result.status("error").msg(e.getMessage());
        }
        return result;
    }

    @GetMapping("/launcher/state")
    public String state(String appId) {
        SparkAppHandle handle = handles.get(appId);
        if (handle == null)
            return "cannot find the app or the app has finished...";
        SparkAppHandle.State state = handle.getState();
        return state.name();
    }

    @GetMapping("/launcher/kill")
    public String kill(String appId) {
        SparkAppHandle handle = handles.get(appId);
        if (handle == null)
            return "cannot find the app: " + appId;
        handle.kill();
        return "the app " + appId + " has been killed";
    }

    @PostMapping("/yarn/submit")
    public Result<String> submitByYarn(@RequestBody String requestBody) {
        JSONObject json = new JSONObject(requestBody);
        String name = json.optString("name");
        String mainClass = json.getString("mainClass");
        String appResource = json.optString("appResource", setting.getJarFile());
        String args = json.optString("args");
        String driverMemory = json.optString("driverMemory", setting.getResourceConfig().getDriverMemory());
        String executorMemory = json.optString("executorMemory", setting.getResourceConfig().getExecutorMemory());
        int numExecutors = json.optInt("numExecutors", setting.getResourceConfig().getNumExecutors());
        int executorCores = json.optInt("executorCores", setting.getResourceConfig().getExecutorCores());
        int parallelism = json.optInt("parallelism", setting.getResourceConfig().getParallelism());
        SparkTask sparkTask = new SparkTask();
        sparkTask.setName(name);
        sparkTask.setMainClass(mainClass);
        sparkTask.setJarFile(appResource);
        sparkTask.setArgs(args);
        sparkTask.setDriverMemory(driverMemory);
        sparkTask.setExecutorMemory(executorMemory);
        sparkTask.setNumExecutors(numExecutors);
        sparkTask.setExecutorCores(executorCores);
        sparkTask.setParallelism(parallelism);
        Result<String> result = new Result<>();
        try {
            sparkService.submit(sparkTask);
            result.status("ok").data(sparkTask.getId());
        } catch (Exception e) {
            log.error("模型任务提交失败", e);
            result.status("error").msg(e.getMessage());
        }
        return result;
    }

    @GetMapping("/yarn/state")
    public Result<SparkTaskState> stateByYarn(String appId) {
        Result<SparkTaskState> result = new Result<>();
        try {
            SparkTaskState state = sparkService.getState(appId);
            result.status("ok").data(state);
        } catch (IOException | YarnException e) {
            log.error("获取spark任务[" + appId + "]状态失败", e);
            result.status("error").msg("获取spark任务状态失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/yarn/kill")
    public Result<String> killByYarn(String appId) {
        Result<String> result = new Result<>();
        try {
            sparkService.kill(appId);
            result.status("ok");
        } catch (IOException | YarnException e) {
            log.error("取消spark任务[" + appId + "]失败", e);
            result.status("error").msg(e.getMessage());
        }
        return result;
    }
}
