package com.uh.framework.dbproxy;

public class DbProxyUtils {

    /**
     * Checks if the given SQL string is a write operation (INSERT, UPDATE, DELETE).
     * @param sql
     * @return
     */
    public static boolean isWriteSql(String sql) {
        if (sql == null || sql.isEmpty()) {
            return false;
        }

        String comparingSql = trimBeginning(sql).substring(0,6).toUpperCase();

        return comparingSql.startsWith("INSERT") ||
               comparingSql.startsWith("UPDATE") ||
               comparingSql.startsWith("DELETE");
    }

    /**
     * Trims leading whitespace from the beginning of the SQL string.
     * @param sql
     * @return
     */
    private static String trimBeginning(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }

        int start = 0;
        while (start < sql.length() && Character.isWhitespace(sql.charAt(start))) {
            start++;
        }

        return sql.substring(start);
    }


}
