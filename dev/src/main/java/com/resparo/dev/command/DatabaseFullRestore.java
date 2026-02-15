package com.resparo.dev.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.service.FullRestoreDatabaseService;
import com.resparo.dev.util.ListDatabaseBackupFile;

@Component
@Command(group = "Restore", description = "Full Restoration of Databases")
public class DatabaseFullRestore {

    @Autowired
    private FullRestoreDatabaseService fullRestoreDatabaseService;

    @Autowired
    private ListDatabaseBackupFile listDatabaseBackupFile;


    @Command(description = "Full Restoration of exisiting Databases", group = "full restore")
    public String fullRestoreDb(
            @Option(longNames = "database", required = true) String dataBaseName,
            @Option(longNames = "type", required = true) DatabaseType dbType,
            @Option(longNames = "username", required = true) String Username,
            @Option(longNames = "host", required = true, defaultValue = "localhost") String host,
            @Option(longNames = "port", required = true) String port){

   
        String backupFilePath = listDatabaseBackupFile.returnPath(Username, dataBaseName);
        return fullRestoreDatabaseService.fullRestoreDb(dataBaseName, dbType, Username, host, port, backupFilePath);
    }

    @Command(description = "Full Restoration of nonexisiting Databases", group = "full restore")
    public String fullRestoreWithoutDb(
            @Option(longNames = "type", required = true) DatabaseType dbType,
            @Option(longNames = "username", required = true) String Username,
            @Option(longNames = "newdb", required = true) String newdataBaseName,
         @Option(longNames = "oldDb", required = true) String olddataBaseName) {
        String backupFilePath = listDatabaseBackupFile.returnPath(Username, olddataBaseName);
        return fullRestoreDatabaseService.fullRestoreWithoutDb(dbType, Username, newdataBaseName , backupFilePath);
    }

    @Command(description = "Drop and Recreate Restoration of Databases", group = "full restore")
    public String dropAndRecreateDb(
            @Option(longNames = "username", required = true) String Username,
            @Option(longNames = "database", required = true) String dataBaseName,
            @Option(longNames = "type", required = true) DatabaseType dbType

    ) {
        String backupFilePath = listDatabaseBackupFile.returnPath(Username, dataBaseName);
        return fullRestoreDatabaseService.dropAndRecreateDb(Username, dataBaseName, dbType , backupFilePath);
    }
}
