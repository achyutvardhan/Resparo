package com.resparo.dev.service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;

import com.resparo.dev.domain.BackupTypes;
import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.util.ConnectionProvider;

@Service
public class DatabaseBackupService {
    @Autowired
    private ConnectionProvider connectionProvider;

    public String backupDb(BackupTypes backupTypes, DatabaseType databaseType, String databaseName) {
        try {
            String output = "";
            String baseDir = "/Users/achyutvardhan/Resparo/dev/storage";
            String fileName = databaseName + "_" + System.currentTimeMillis() + ".dump";
            Path backupPath = Paths.get(baseDir, fileName);
            new File(baseDir).mkdirs();
            String user = connectionProvider.getUserName();
            if (backupTypes == BackupTypes.FULL && connectionProvider.isConnected()) {
                if (databaseType == DatabaseType.POSTGRESQL) {
                    output = new ProcessExecutor()
                            .command("pg_dump", "-Fc", "-h", "localhost",
                                    "-U", user, "-f", backupPath.toString(), databaseName)
                            .readOutput(true)
                            .execute()
                            .outputUTF8();
                } else if (databaseType == DatabaseType.MYSQL) {
                    output = new ProcessExecutor()
                            .command("mysqldump", "--single-transaction", databaseName)
                            .readOutput(true)
                            .execute()
                            .outputUTF8();
                } else {
                    throw new Exception();
                }
            } else if (backupTypes == BackupTypes.DIFFERENTIAL) {

            } else {

            }
            return output;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
