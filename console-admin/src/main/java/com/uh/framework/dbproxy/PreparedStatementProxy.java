package com.uh.framework.dbproxy;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

public class PreparedStatementProxy extends StatementProxy implements PreparedStatement {

    private final PreparedStatement ps;

    private StatementOperationManager operationManager = StatementOperationManager.getInstance();

    private StatementOperation operation;

    public PreparedStatementProxy(PreparedStatement preparedStatement, String sql) {
        super(preparedStatement);
        this.ps = preparedStatement;
        this.operation = new StatementOperation(sql);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return ps.executeQuery();
    }

    @Override
    public int executeUpdate() throws SQLException {
        int ret = ps.executeUpdate();
        operationManager.addOperation(operation);
        return ret;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        operation.addParameter(new StatementParameter("setNull", parameterIndex, sqlType));
        ps.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        operation.addParameter(new StatementParameter("setBoolean", parameterIndex, Types.BOOLEAN, x));
        ps.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        operation.addParameter(new StatementParameter("setByte", parameterIndex, Types.TINYINT, x));
        ps.setByte(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        operation.addParameter(new StatementParameter("setShort", parameterIndex, Types.SMALLINT, x));
        ps.setShort(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        operation.addParameter(new StatementParameter("setInt", parameterIndex, Types.INTEGER, x));
        ps.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        operation.addParameter(new StatementParameter("setLong", parameterIndex, Types.BIGINT, x));
        ps.setLong(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        operation.addParameter(new StatementParameter("setFloat", parameterIndex, Types.FLOAT, x));
        ps.setFloat(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        operation.addParameter(new StatementParameter("setDouble", parameterIndex, Types.DOUBLE, x));
        ps.setDouble(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        operation.addParameter(new StatementParameter("setBigDecimal", parameterIndex, Types.DECIMAL, x));
        ps.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        operation.addParameter(new StatementParameter("setString", parameterIndex, Types.VARCHAR, x));
        ps.setString(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        operation.addParameter(new StatementParameter("setBytes", parameterIndex, Types.BINARY, x));
        ps.setBytes(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        operation.addParameter(new StatementParameter("setDate", parameterIndex, Types.DATE, x));
        ps.setDate(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        operation.addParameter(new StatementParameter("setTime", parameterIndex, Types.TIME, x));
        ps.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        operation.addParameter(new StatementParameter("setTimestamp", parameterIndex, Types.TIMESTAMP, x));
        ps.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        logger.error("Do not support setAsciiStream with length parameter!");
        ps.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        logger.error("Do not support setUnicodeStream with length parameter!");
        ps.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        logger.error("Do not support setBinaryStream with length parameter!");
        ps.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        operation.addParameter(new StatementParameter("setObject", parameterIndex, Types.OTHER, x));
        ps.setObject(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        logger.error("Do not support setObject with targetSqlType parameter!");
        ps.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void clearParameters() throws SQLException {
        operation.cleanParameters();
        ps.clearParameters();
    }

    @Override
    public boolean execute() throws SQLException {
        boolean ret = ps.execute();
        operationManager.addOperation(operation);
        return ret;
    }

    @Override
    public void addBatch() throws SQLException {
        logger.error("Do not support batch operations!");
        ps.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        logger.error("Do not support setCharacterStream with length parameter!");
        ps.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        logger.error("Do not support setRef!");
        ps.setRef(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        logger.error("Do not support setBlob!");
        ps.setBlob(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        logger.error("Do not support setClob!");
        ps.setClob(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        operation.addParameter(new StatementParameter("setArray", parameterIndex, Types.ARRAY, x));
        ps.setArray(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return ps.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        logger.error("Do not support setDate with Calendar parameter!");
        ps.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        logger.error("Do not support setTime with Calendar parameter!");
        ps.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        logger.error("Do not support setTimestamp with Calendar parameter!");
        ps.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        logger.error("Do not support setNull with typeName parameter!");
        ps.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        logger.error("Do not support setURL!");
        ps.setURL(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return ps.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        logger.error("Do not support setRowId!");
        ps.setRowId(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        logger.error("Do not support setNString!");
        ps.setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        logger.error("Do not support setNCharacterStream with length parameter!");
        ps.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        logger.error("Do not support setNClob!");
        ps.setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        logger.error("Do not support setClob with length parameter!");
        ps.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        logger.error("Do not support setBlob with length parameter!");
        ps.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        logger.error("Do not support setNClob with length parameter!");
        ps.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        logger.error("Do not support setSQLXML!");
        ps.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        logger.error("Do not support setObject with targetSqlType and scaleOrLength parameters!");
        ps.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        logger.error("Do not support setAsciiStream with length parameter!");
        ps.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        logger.error("Do not support setBinaryStream with length parameter!");
        ps.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        logger.error("Do not support setCharacterStream with length parameter!");
        ps.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        logger.error("Do not support setAsciiStream without length parameter!");
        ps.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        logger.error("Do not support setBinaryStream without length parameter!");
        ps.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        logger.error("Do not support setCharacterStream without length parameter!");
        ps.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        logger.error("Do not support setNCharacterStream without length parameter!");
        ps.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        logger.error("Do not support setClob without length parameter!");
        ps.setClob(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        logger.error("Do not support setBlob without length parameter!");
        ps.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        logger.error("Do not support setNClob without length parameter!");
        ps.setNClob(parameterIndex, reader);
    }

}
