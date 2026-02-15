package com.resparo.dev.service;

import java.io.FileInputStream;

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
                    output = new ProcessExecutor()
                            .command("mysql", "-u", Username, "-p", dataBaseName)
                            .redirectInput(new FileInputStream(optedBackupfile))
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue() == 0 ? "Restore successful" : "Restore failed";
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
                    System.out.println("Creating the new Database");
                    int creationOfDatbase = new ProcessExecutor()
                            .command("mysql", "-u", Username, "-p",
                                    "-e", "CREATE DATABASE `" + newDataBaseName + "`;")
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute().getExitValue();
                    System.out.println((creationOfDatbase == 0) ? "Database created" + "->" + newDataBaseName
                            : "Database creation failed");
                    if (creationOfDatbase == 0)
                        output = new ProcessExecutor()
                                .command("mysql", "-u", Username, "-p", newDataBaseName)
                                .redirectInput(new FileInputStream(optedBackupfile))
                                .redirectOutput(System.out)
                                .redirectError(System.err)
                                .execute()
                                .getExitValue() == 0 ? "Restore successful" : "Restore failed";
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
                    System.out.println("Dropping the Database");
                    int dropping = new ProcessExecutor()
                            .command("mysql", "-u", Username, "-p",
                                    "-e", "DROP DATABASE IF EXISTS `" + dataBaseName + "`;")
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute().getExitValue();
                    if (dropping == 0) {
                        System.out.println("Creating the Database");
                        int creationOfDatbase = new ProcessExecutor()
                                .command("mysql", "-u", Username, "-p",
                                        "-e", "CREATE DATABASE `" + dataBaseName + "`;")
                                .redirectOutput(System.out)
                                .redirectError(System.err)
                                .execute().getExitValue();
                        if (creationOfDatbase == 0)
                            System.out.println("Restoring the Database");
                        output = new ProcessExecutor()
                                .command("mysql", "-u", Username, "-p", dataBaseName)
                                .redirectInput(new FileInputStream(optedBackupfile))
                                .redirectOutput(System.out)
                                .redirectError(System.err)
                                .execute()
                                .getExitValue() == 0 ? "Database Dropped and Restored successfully "
                                        : "Database Dropped and Restored failed ";
                    }
                }
            }
            return output;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
