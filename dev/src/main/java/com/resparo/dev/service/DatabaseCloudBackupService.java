package com.resparo.dev.service;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;

import com.resparo.dev.domain.BackupTypes;
import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.util.ConnectionProvider;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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
                    log.debug("Full");
                    switch (dbType) {
                        case POSTGRESQL -> {
                            String fileName = user + "_" + databaseName + "_" + System.currentTimeMillis() + ".dump";
                            Path tempFile = Files.createTempFile("pgdump-", ".dump");

                            try {
                                StartedProcess startedProcess = new ProcessExecutor()
                                        .command("pg_dump", "-Fc", "-h", "localhost", "-U", user, databaseName)
                                        .redirectOutput(Files.newOutputStream(tempFile)) // write directly to disk
                                        .redirectError(new LogOutputStream() {
                                            @Override
                                            protected void processLine(String line) {
                                                log.error("[pg_dump] {}", line);
                                            }
                                        })
                                        .start();

                                ProcessResult result = startedProcess.getFuture().get();

                                if (result.getExitValue() != 0) {
                                    return "pg_dump process failed with exit code: " + result.getExitValue();
                                }

                                log.info("pg_dump finished, file size: {} bytes", Files.size(tempFile));

                                // Stream from stable temp file — no pipe issues at all
                                try (InputStream fileStream = Files.newInputStream(tempFile)) {
                                    output = s3service.cloudBackup(fileStream, fileName);
                                }

                            }catch(Exception e){
                                log.error(e.getMessage());
                            } finally {
                                Files.deleteIfExists(tempFile); // always clean up
                            }
                        }
                        case MYSQL -> {
                            log.debug("calls reaching the backupDb");
                            String[] username = user.split("@");
                            String fileName = username[0] + "_" + databaseName + "_" + System.currentTimeMillis()
                                    + ".sql";
                            Path tempFile = Files.createTempFile("mysqldump-", ".sql");

                            try {
                                StartedProcess startedProcess = new ProcessExecutor()
                                        .command("mysqldump",
                                                "--single-transaction",
                                                "--set-gtid-purged=OFF",
                                                "-u", username[0],
                                                "-p",
                                                databaseName)
                                        .redirectOutput(Files.newOutputStream(tempFile))
                                        .redirectError(new LogOutputStream() {
                                            @Override
                                            protected void processLine(String line) {
                                                log.error("[mysqldump] {}", line);
                                            }
                                        })
                                        .start();

                                ProcessResult result = startedProcess.getFuture().get();

                                if (result.getExitValue() != 0) {
                                    return "mysqldump process failed with exit code: " + result.getExitValue();
                                }

                                log.info("mysqldump finished, file size: {} bytes", Files.size(tempFile));

                                try (InputStream fileStream = Files.newInputStream(tempFile)) {
                                    output = s3service.cloudBackup(fileStream, fileName);
                                }

                            }catch(Exception e){
                                log.error(e.getMessage());
                            } finally {
                                Files.deleteIfExists(tempFile);
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
