package com.uh.framework.dbproxy;

import java.sql.Types;

public class StatementParameter {
    private String method;
    private int index;
    private int sqlType;
    private Object value;

    public StatementParameter(int index) {
        this.index = index;
    }

    public StatementParameter(String method, int index, int sqlType) {
        this.method = method;
        this.index = index;
        this.sqlType = sqlType;
        this.value = null;
    }

    public StatementParameter(String method, int index, int sqlType, Object value) {
        this.method = method;
        this.index = index;
        this.sqlType = sqlType;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public int getSqlType() {
        return sqlType;
    }

    public String getMethod() {
        return method;
    }

    public String getSqlTypeName() {
        if(sqlType == 0) {
            return "UNKNOWN";
        }

        switch (sqlType) {
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.CHAR:
                return "CHAR";
            case Types.VARCHAR:
                return "VARCHAR";
            case Types.TINYINT:
                return "TINYINT";
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.INTEGER:
                return "INTEGER";
            case Types.BIGINT:
                return "BIGINT";
            case Types.FLOAT:
                return "FLOAT";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.DECIMAL:
                return "DECIMAL";
            case Types.BINARY:
                return "BINARY";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.OTHER:
                return "OTHER";
            default:
                return "UNKNOWN";
        }
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        if(index == 0) {
           return "{index=0, EMPTY}";
        }
        return "{index=" + index +
                ", method='" + method + '\'' +
                ", sqlType=" + getSqlTypeName() +
                ", value=" + value +
                '}';
    }
}
