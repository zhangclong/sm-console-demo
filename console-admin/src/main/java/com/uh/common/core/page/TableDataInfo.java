package com.uh.common.core.page;

import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 *
 * @author Zhang Chenlong
 */
public class TableDataInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 列表数据 */
    private List<?> rows;

    /** 消息状态码 */
    private int code;

    /** 消息内容 */
    private String msg;

    /**
     * 表格数据对象
     */
    public TableDataInfo()
    {
    }

    /**
     * 分页
     *
     * @param list 列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<?> list, int total)
    {
        this.rows = list;
        this.total = total;
    }

    // 获取total变量
    public long getTotal()
    {
        return total;
    }

    // 设置total变量
    public void setTotal(long total)
    {
        this.total = total;
    }

    // 获取rows变量
    public List<?> getRows()
    {
        return rows;
    }

    // 设置rows变量
    public void setRows(List<?> rows)
    {
        this.rows = rows;
    }

    // 获取code变量
    public int getCode()
    {
        return code;
    }

    // 设置code变量
    public void setCode(int code)
    {
        this.code = code;
    }

    // 获取msg变量
    public String getMsg()
    {
        return msg;
    }

    // 设置msg变量
    public void setMsg(String msg)
    {
        this.msg = msg;
    }
}
