package com.resparo.dev.service;

import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;

import com.resparo.dev.domain.DatabaseType;

@Service
public class FullRestoreDatabaseService {

    public String fullRestoreDb(String dataBaseName, DatabaseType dbType, String Username, String host, String port,
            String optedBackupfile) {
        try {

            String output = "";
            switch (dbType) {
                case POSTGRESQL -> {
                    output = new ProcessExecutor()
                            .command("pg_restore", "-U", Username, "-d", dataBaseName, "-h", host, "-p", port,
                                    optedBackupfile)
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue() == 0 ? "Restore successful" : "Restore failed";
                }
                case MYSQL -> {
                }
            }
            return output;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String fullRestoreWithoutDb(DatabaseType dbType, String Username, String newDataBaseName,
            String optedBackupfile) {
        try {
            String output = "";
            switch (dbType) {
                case POSTGRESQL -> {
                    int creationOfDatabase = new ProcessExecutor()
                            .command("createdb", "-U", Username, newDataBaseName)
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue();
                    System.out.println((creationOfDatabase == 0) ? "Database created" + "->" + newDataBaseName
                            : "Database creation failed");
                    output = new ProcessExecutor()
                            .command("pg_restore", "-U", Username, "-d", newDataBaseName, optedBackupfile)
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue() == 0 ? "Restore successful" : "Restore failed";
                }
                case MYSQL -> {
                }
            }
            return output;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String dropAndRecreateDb(String Username, String dataBaseName, DatabaseType dbType, String optedBackupfile) {
        try {
            String output = "";
            switch (dbType) {
                case POSTGRESQL -> {
                    output = new ProcessExecutor()
                            .command("pg_restore", "--clean", "--if-exists", "-U", Username, "-d", dataBaseName,
                                    optedBackupfile)
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue() == 0 ? "Database Dropped and Restored successfully "
                                    : "Database Dropped and Restored failed ";

                }

                case MYSQL -> {
                }
            }
            return output;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
