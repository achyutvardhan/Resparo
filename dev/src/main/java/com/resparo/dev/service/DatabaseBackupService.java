package com.resparo.dev.service;

import java.io.FileOutputStream;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessOutput;

import com.resparo.dev.domain.BackupTypes;
import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.util.ConnectionProvider;
import com.resparo.dev.util.FileNameProvider;
import com.resparo.dev.util.PgbackrestInstalled;

@Service
public class DatabaseBackupService {
    @Autowired
    private ConnectionProvider connectionProvider;
    

    public String backupDb(BackupTypes backupTypes, DatabaseType databaseType, String databaseName) {
        try {
            String output = "";
            String user = connectionProvider.getUserName();
            Path backupPath = FileNameProvider.provideFileName(user, databaseType, databaseName);
            if (backupTypes == BackupTypes.FULL && connectionProvider.isConnected()) {
                if (databaseType == DatabaseType.POSTGRESQL) {
                    output = new ProcessExecutor()
                            .command("pg_dump", "-Fc", "-h", "localhost",
                                    "-U", user, "-f", backupPath.toString(), databaseName)
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue() == 0 ? "Backup successful" : "Backup failed";

                } else if (databaseType == DatabaseType.MYSQL) {
                    String[] username = user.split("@");
                    output = new ProcessExecutor()
                            .command("mysqldump",
                                    "--single-transaction",
                                    "-u", username[0],
                                    "-p",
                                    databaseName)
                            .redirectOutput(new FileOutputStream(backupPath.toFile()))
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue() == 0 ? "Backup successful" : "Backup failed";
                } else { 
                    throw new Exception();
                }
            } else if (backupTypes == BackupTypes.DIFFERENTIAL) {
                switch (databaseType) {
                    case POSTGRESQL -> {
                        if(PgbackrestInstalled.checkInstallation())
                        output = new ProcessExecutor()
                                        .command("pgbackrest", "--stanza=main" , "backup" , "--type=diff")
                                        .redirectOutput(System.out)
                                        .redirectError(System.err) 
                                        .execute()
                                        .getExitValue() == 0 ? "Backup successful" : "Backup failed";
                        else{
                           InstalltionBackupService installtionBackupService = new InstalltionBackupService(databaseType);
                           installtionBackupService.afterInstalltionMannualForPgBackrest();
                        }
                    }
                    case MYSQL -> {}
                };
            } else {
                switch (databaseType) {
                    case POSTGRESQL -> {
                        if(PgbackrestInstalled.checkInstallation())
                        output = new ProcessExecutor()
                                        .command("pgbackrest", "--stanza=main" , "backup" , "--type=incr")
                                        .redirectOutput(System.out)
                                        .redirectError(System.err) 
                                        .execute()
                                        .getExitValue() == 0 ? "Backup successful" : "Backup failed";   
                        else{
                           InstalltionBackupService installtionBackupService = new InstalltionBackupService(databaseType);
                           installtionBackupService.afterInstalltionMannualForPgBackrest();
                        }
                    }
                    case MYSQL -> {}
                };
            }
            return output;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
