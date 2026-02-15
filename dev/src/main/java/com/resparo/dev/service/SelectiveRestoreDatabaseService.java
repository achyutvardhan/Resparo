package com.resparo.dev.service;

import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;

import com.resparo.dev.domain.DatabaseType;

@Service
public class SelectiveRestoreDatabaseService {

    public String tableRestoration(String databaseName, DatabaseType dbTye, String Username, String table_Name,
            String optedBackupfile) {
        try {
            String output = "";
            switch (dbTye) {
                case POSTGRESQL -> {
                    output = new ProcessExecutor()
                            .command("pg_restore", "-U", Username, "-d", databaseName, "-t", table_Name,
                                    optedBackupfile)
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue() == 0 ? "Table Restore successful" : "Table Restore failed";
                }
                case MYSQL -> {
                    output = "Selective Table Restoration is not available for MYSQL";
                }
            }
            return output;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String schemaRestoration(String databaseName, String Username, DatabaseType dbTye, String schema_Name,
            String optedBackupfile) {
        try {
            String output = "";
            switch (dbTye) {
                case POSTGRESQL -> {
                    output = new ProcessExecutor()
                            .command("pg_restore", "-U", Username, "-d", databaseName, "-n", schema_Name,
                                    optedBackupfile)
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue() == 0 ? "Schema Restore successful" : "Schema Restore failed";
                }
                case MYSQL -> {
                    output = "Selective Schema Restoration is not available for MYSQL";
                }
            }
            return output;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
