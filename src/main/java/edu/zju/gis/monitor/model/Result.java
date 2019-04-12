package edu.zju.gis.monitor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author yanlong_lee@qq.com
 * @version 1.0 2019/04/12
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    String status;
    String msg;
    T data;

    public Result<T> status(String status) {
        this.status = status;
        return this;
    }

    public Result<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }
}
