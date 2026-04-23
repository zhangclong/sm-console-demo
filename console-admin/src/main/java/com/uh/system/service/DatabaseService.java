package com.uh.system.service;

import java.io.File;
import java.util.List;

public interface DatabaseService {

    /**
     * 执行classpath中的SQL文件
     * @param classPathSql
     */
    void executeSql(String classPathSql);

    /**
     * 返回备份的文件（MySQL 方式：通过外部 mysqldump 命令生成 gzip 压缩的 SQL 文件）
     * @return
     */
    File backupDatabase();

    /**
     * 返回被删除的文件
     *
     * @return
     */
    List<File> cleanExpiredBackup(int maxHistory);

    /**
     *
     * @param fileName 导入文件名（mysqldump 导出的 .sql.gz 或 .sql 文件）
     */
    void restoreDatabase(String fileName);

}
