package edu.zju.gis.monitor;

import edu.zju.gis.monitor.config.CommonSetting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SparkMonitorAppTests {
    @Autowired
    CommonSetting setting;

    @Test
    public void contextLoads() {
        System.out.println(setting.getResourceConfig());
    }

}
