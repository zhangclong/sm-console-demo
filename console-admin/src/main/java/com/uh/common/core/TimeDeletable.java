package com.uh.common.core;

import java.util.Date;

/**
 * 基于时间条件进行删除的接口
 */
public interface TimeDeletable {

    /**
     * 删除小于 createTime 参数时间之前的数据
     * @param createTime
     * @return
     */
     int deleteByCreateTime(Date createTime);
}
