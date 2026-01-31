package com.resparo.dev.service;

import java.io.FileOutputStream;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;

import com.resparo.dev.domain.BackupTypes;
import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.util.ConnectionProvider;
import com.resparo.dev.util.FileNameProvider;

@Service
public class DatabaseBackupService {
    @Autowired
    private ConnectionProvider connectionProvider;

    public String backupDb(BackupTypes backupTypes, DatabaseType databaseType, String databaseName) {
        try {
            String output = "";
            String user = connectionProvider.getUserName();
            if (backupTypes == BackupTypes.FULL && connectionProvider.isConnected()) {
                if (databaseType == DatabaseType.POSTGRESQL) {
                    Path backupPath = FileNameProvider.provideFileName(user, databaseType, databaseName);
                    output = new ProcessExecutor()
                            .command("pg_dump", "-Fc", "-h", "localhost",
                                    "-U", user, "-f", backupPath.toString(), databaseName)
                            .readOutput(true)
                            .execute()
                            .getExitValue() == 0 ? "Backup successful" : "Backup failed";

                } else if (databaseType == DatabaseType.MYSQL) {
                    Path backupPath = FileNameProvider.provideFileName(user, databaseType, databaseName);
                    String[] username = user.split("@");
                    output = new ProcessExecutor()
                            .command("mysqldump",
                                    "--single-transaction",
                                    "-u", username[0],
                                    "-p",
                                    databaseName)
                            .redirectOutput(new FileOutputStream(backupPath.toFile()))
                            .redirectError(System.err)
                            .readOutput(true)
                            .execute()
                            .getExitValue() == 0 ? "Backup successful" : "Backup failed";
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
