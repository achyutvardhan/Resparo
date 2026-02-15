package com.resparo.dev.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.service.SelectiveRestoreDatabaseService;
import com.resparo.dev.util.ListDatabaseBackupFile;

@Component
@Command(group = "Selective Restore", description = "Selective Restoration of Databases")
public class DatabaseSelectiveRestore {
    @Autowired
    private SelectiveRestoreDatabaseService selectiveRestoreDatabaseService;

    @Autowired
    private ListDatabaseBackupFile listDatabaseBackupFile;

    @Command(description = "Table Restoration of Databases", group = "selective restore")
    public String tableRestoreDb(
            @Option(longNames = "database", required = true) String dataBaseName,
            @Option(longNames = "type", required = true) DatabaseType dbType,
            @Option(longNames = "username", required = true) String Username,
            @Option(longNames = "table-name", required = true) String tableName) {
        String backupFilePath = listDatabaseBackupFile.returnPath(Username, dataBaseName);
        return selectiveRestoreDatabaseService.tableRestoration(dataBaseName, dbType, Username, tableName,
                backupFilePath);
    }

    @Command(description = "Schema Restoration of Databases", group = "selective restore")
    public String schemaRestoreDb(
            @Option(longNames = "database", required = true) String dataBaseName,
            @Option(longNames = "type", required = true) DatabaseType dbType,
            @Option(longNames = "username", required = true) String Username,
            @Option(longNames = "schema-name", required = true) String schemaName) {
        String backupFilePath = listDatabaseBackupFile.returnPath(Username, dataBaseName);
        return selectiveRestoreDatabaseService.schemaRestoration(dataBaseName, Username, dbType, schemaName,
                backupFilePath);
    }
    @Command(description = "PgBackRest Point in time Restoration of Postgresql Databases", group = "selective restore")
    public String restorePITR(
            @Option(longNames = "type", required = true, defaultValue = "POSTGRESQL") DatabaseType dbType,
            @Option(longNames = "time", required = true) String time,
            @Option(longNames = "stanza", required = true) String stanza
        ) {
        return selectiveRestoreDatabaseService.retorePITR(dbType, time , stanza);
    }
}
