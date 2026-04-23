package com.uh.framework.dbproxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class StatementOperation {
    private String sql;
    private List<StatementParameter> parameterList;
    private int maxParameterIndex = 0;

    public StatementOperation() {
    }


    public StatementOperation(String sql) {
        this.sql = sql;
        this.parameterList = new ArrayList<>();
        //因为在JDBC的PreparedStatement中，参数索引从1开始，所以这里初始化为0
        this.parameterList.add(0, new StatementParameter(0));
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<StatementParameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<StatementParameter> parameterList) {
        this.parameterList = parameterList;
    }

    public int getMaxParameterIndex() {
        return maxParameterIndex;
    }

    public void setMaxParameterIndex(int maxParameterIndex) {
        this.maxParameterIndex = maxParameterIndex;
    }

    public void addParameter(StatementParameter parameter) {
        this.parameterList.add(parameter);
        //更新最大参数索引
        if (parameter.getIndex() > maxParameterIndex) {
            maxParameterIndex = parameter.getIndex();
        }
    }

    public void cleanParameters() {
        this.parameterList.clear();
        this.maxParameterIndex = 0;
    }

    public StatementParameter[] toParameters() {
        StatementParameter[] parameters = new StatementParameter[maxParameterIndex + 1];
        for(StatementParameter parameter : parameterList) {
            parameters[parameter.getIndex()] = parameter;
        }
        return parameters;
    }

    @Override
    public String toString() {
        String params = "";
        if(parameterList.size() > 1) {
            StatementParameter[] orgParams = toParameters();
            StatementParameter[] subParams = Arrays.copyOfRange(orgParams, 1, orgParams.length);// 去掉orgParameters的第一元素
            params = ", parameters=" + Arrays.toString(subParams);
        }

        return "{sql='" + sql + '\'' + params  + '}';
    }
}
