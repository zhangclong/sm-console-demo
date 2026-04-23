package com.uh.framework.dbproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;

import static com.uh.framework.dbproxy.DbProxyUtils.isWriteSql;

public class ConnectionProxy {

    public static Connection createProxy(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                connection.getClass().getClassLoader(),
                new Class[]{Connection.class},
                new ConnectionHandler(connection)
        );
    }

    private static class ConnectionHandler implements InvocationHandler {

        private StatementOperationManager manager = StatementOperationManager.getInstance();

        private final Connection connection;

        public ConnectionHandler(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(manager.isActiveProxy()) {
                if ("createStatement".equals(method.getName())) {
                    Statement statement = (Statement) method.invoke(connection, args);
                    return new StatementProxy(statement);
                } else if ("prepareStatement".equals(method.getName())) {
                    if (args != null && args.length > 0 && args[0] instanceof String) {
                        String sql = (String) args[0];
                        if (isWriteSql(sql)) {
                            PreparedStatement preparedStatement = (PreparedStatement) method.invoke(connection, args);
                            return new PreparedStatementProxy(preparedStatement, sql);
                        }
                    }
                }
            }
            return method.invoke(connection, args);
        }
    }
}
