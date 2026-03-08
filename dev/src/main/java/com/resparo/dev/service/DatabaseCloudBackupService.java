package com.resparo.dev.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;

import com.resparo.dev.domain.BackupTypes;
import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.util.ConnectionProvider;

@Service
public class DatabaseCloudBackupService {

    @Autowired
    private S3service s3service;

    @Autowired
    private ConnectionProvider connectionProvider;

    public String backupDb(BackupTypes bkpType, DatabaseType dbType, String databaseName) {
        String output = "";
        try {
            String user = connectionProvider.getUserName();
            switch (bkpType) {
                case FULL -> {
                    switch (dbType) {
                        case POSTGRESQL -> {
                            String fileName = user + "_" + databaseName + "_" + System.currentTimeMillis() + ".dump";
                            Process process = new ProcessExecutor()
                                    .command("pg_dump", "-Fc", "-h", "localhost", "-U", user, databaseName)
                                    .redirectErrorStream(true)
                                    .start()
                                    .getProcess();
                            InputStream pgDumpStream = process.getInputStream();

                            output = s3service.cloudBackup(pgDumpStream, fileName);
                            int exitCode = process.waitFor();
                            if (exitCode != 0) {
                                return "pg_dump process failed with exit code: " + exitCode;
                            }
                        }
                        case MYSQL -> {
                            String[] username = user.split("@");
                            String fileName = username[0] + "_" + databaseName + "_" + System.currentTimeMillis()
                                    + ".sql";
                            Process process = new ProcessExecutor()
                                    .command("mysqldump",
                                            "--single-transaction",
                                            "-u", username[0],
                                            "-p",
                                            databaseName)
                                    .redirectErrorStream(true)
                                    .start()
                                    .getProcess();
                            InputStream mysqlDumpStream = process.getInputStream();

                            output = s3service.cloudBackup(mysqlDumpStream, fileName);
                            int exitCode = process.waitFor();
                            if (exitCode != 0) {
                                return "pg_dump process failed with exit code: " + exitCode;
                            }
                        }
                    }
                }
                case DIFFERENTIAL -> {
                    switch (dbType) {
                        case POSTGRESQL -> {
                            output = new ProcessExecutor()
                                    .command("pgbackrest", "--stanza=main", "backup", "--type=diff")
                                    .redirectOutput(System.out)
                                    .redirectError(System.err)
                                    .execute()
                                    .getExitValue() == 0 ? "Backup successful" : "Backup failed";
                        }
                        case MYSQL -> {
                            output = "Diffferential backup is not available for MySql";
                        }
                    }
                }
                case INCREMENTAL -> {
                    switch (dbType) {
                        case POSTGRESQL -> {
                            output = new ProcessExecutor()
                                    .command("pgbackrest", "--stanza=main", "backup", "--type=incr")
                                    .redirectOutput(System.out)
                                    .redirectError(System.err)
                                    .execute()
                                    .getExitValue() == 0 ? "Backup successful" : "Backup failed";
                        }
                        case MYSQL -> {
                            output = "Incremental backup is not available for MySql";
                        }
                    }
                }
            }
            return output;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
