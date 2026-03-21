package com.resparo.dev.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.service.SelectiveRestoreDatabaseService;
import com.resparo.dev.util.ListDatabaseBackupFile;

/**
 * DatabaseSelectiveRestore component provides selective restoration
 * capabilities for databases.
 * 
 * This class handles partial database restoration operations including
 * table-level, schema-level,
 * and point-in-time recovery (PITR) for supported database types. It integrates
 * with backup
 * file management and database restoration services to enable granular recovery
 * of database objects.
 * 
 * Supported restoration types:
 * - Table Restoration: Restore individual tables from database backups
 * - Schema Restoration: Restore entire schemas from database backups
 * - Point-in-Time Recovery (PITR): Restore PostgreSQL databases to a specific
 * point in time using PgBackRest
 * 
 * @author Resparo Development Team
 * @version 1.0
 */

@Component
@Command(group = "Selective Restore", description = "Selective Restoration of Databases")
public class DatabaseSelectiveRestore {
    @Autowired
    private SelectiveRestoreDatabaseService selectiveRestoreDatabaseService;

    @Autowired
    private ListDatabaseBackupFile listDatabaseBackupFile;

    /**
     * Restores a specific table from a database backup.
     *
     * @param dataBaseName The name of the database containing the table to restore.
     *                     Required.
     * @param dbType       The type of the database (e.g., MySQL, PostgreSQL).
     *                     Required.
     * @param Username     The username used for authentication with the database.
     *                     Required.
     * @param tableName    The name of the table to restore. Required.
     * @return A status message indicating the result of the table restoration
     *         operation.
     */
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

    /**
     * Restores a specific schema from a database backup.
     *
     * @param dataBaseName The name of the database containing the schema to
     *                     restore. Required.
     * @param dbType       The type of the database (e.g., MySQL, PostgreSQL).
     *                     Required.
     * @param Username     The username used for authentication with the database.
     *                     Required.
     * @param schemaName   The name of the schema to restore. Required.
     * @return A status message indicating the result of the schema restoration
     *         operation.
     */
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

    /**
     * Restores a PostgreSQL database to a specific point in time using PgBackRest.
     * Supports restoration from both local and cloud-based backups.
     *
     * @param dbType The type of the database. Defaults to POSTGRESQL.
     * @param time   The point in time to restore to. Required. Format should be
     *               compatible with PgBackRest.
     * @param stanza The PgBackRest stanza name identifying the backup
     *               configuration. Required.
     * @return A status message indicating the result of the point-in-time recovery
     *         operation.
     */
    @Command(description = "Restore PostgreSQL database to a specific point in time using PgBackRest from local or cloud backup", group = "local selective restore")
    public String restorePITR(
            @Option(longNames = "type", required = true, defaultValue = "POSTGRESQL", description = "Type of the database") DatabaseType dbType,
            @Option(longNames = "time", required = true, description = "Point in time to restore to") String time,
            @Option(longNames = "stanza", required = true, description = "PgBackRest stanza name") String stanza) {
        return selectiveRestoreDatabaseService.retorePITR(dbType, time, stanza);
    }
}
