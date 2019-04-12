package edu.zju.gis.monitor.mapper;

import java.io.Serializable;

/**
 * @author yanlong_lee@qq.com
 * @version 1.0 2019/04/12
 */
public interface BaseMapper<T, K extends Serializable> {
    T selectByPrimaryKey(K key);

    int insertSelective(T t);

    int deleteByPrimaryKey(K key);

    int updateSelectiveByPrimaryKey(T t);
}
