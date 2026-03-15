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

    @Command(description = "Table Restoration of Databases", group = "local selective restore")
    public String tableRestoreDb(
            @Option(longNames = "database", required = true, description = "Name of the database to restore") String dataBaseName,
            @Option(longNames = "type", required = true, description = "Type of the database") DatabaseType dbType,
            @Option(longNames = "username", required = true, description = "Username for database access") String Username,
            @Option(longNames = "table-name", required = true, description = "Name of the table to restore") String tableName) {
        String backupFilePath = listDatabaseBackupFile.returnPath(Username, dataBaseName);
        return selectiveRestoreDatabaseService.tableRestoration(dataBaseName, dbType, Username, tableName,
                backupFilePath);
    }

    @Command(description = "Schema Restoration of Databases", group = "local selective restore")
    public String schemaRestoreDb(
            @Option(longNames = "database", required = true, description = "Name of the database to restore") String dataBaseName,
            @Option(longNames = "type", required = true, description = "Type of the database") DatabaseType dbType,
            @Option(longNames = "username", required = true, description = "Username for database access") String Username,
            @Option(longNames = "schema-name", required = true, description = "Name of the schema to restore") String schemaName) {
        String backupFilePath = listDatabaseBackupFile.returnPath(Username, dataBaseName);
        return selectiveRestoreDatabaseService.schemaRestoration(dataBaseName, Username, dbType, schemaName,
                backupFilePath);
    }
    @Command(description = "PgBackRest Point in time Restoration of Postgresql Databases", group = "local selective restore")
    public String restorePITR(
            @Option(longNames = "type", required = true, defaultValue = "POSTGRESQL", description = "Type of the database") DatabaseType dbType,
            @Option(longNames = "time", required = true, description = "Point in time to restore to") String time,
            @Option(longNames = "stanza", required = true, description = "PgBackRest stanza name") String stanza
        ) {
        return selectiveRestoreDatabaseService.retorePITR(dbType, time , stanza);
    }
}
