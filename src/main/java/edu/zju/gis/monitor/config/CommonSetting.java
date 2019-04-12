package edu.zju.gis.monitor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author yanlong_lee@qq.com
 * @version 1.0 2019/04/08
 */
@Data
@Configuration
@ConfigurationProperties("setting")
public class CommonSetting {
    String hadoopHome;
    String sparkHome;
    String jarFile;
    String sparkYarnJars;
    ResourceConfig resourceConfig;
}
