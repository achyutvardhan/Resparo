package com.resparo.dev.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.service.FullRestoreDatabaseService;
import com.resparo.dev.service.S3service;
import com.resparo.dev.util.ListDatabaseBackupFile;

@Component
@Command(group = "Restore", description = "Full Restoration of Databases")
public class DatabaseFullRestore {

    @Autowired
    private FullRestoreDatabaseService fullRestoreDatabaseService;

    @Autowired
    private ListDatabaseBackupFile listDatabaseBackupFile;

    @Autowired
    private S3service s3service;

    // ----------------------- Restoration from Local backup -----------------------
    /**
     * Restores an existing database from a local backup file.
     *
     * @param dataBaseName The name of the database to restore.
     * @param dbType       The type of the database (e.g., MySQL, PostgreSQL).
     * @param Username     The username used for database authentication.
     * @param host         The host address of the database server (default is
     *                     "localhost").
     * @param port         The port number of the database server.
     * @return A status message indicating the result of the restore operation.
     */
    @Command(description = "Restore an existing database from a local backup file", group = "local full restore")
    public String fullRestoreDb(
            @Option(longNames = "database", required = true) String dataBaseName,
            @Option(longNames = "type", required = true) DatabaseType dbType,
            @Option(longNames = "username", required = true) String Username,
            @Option(longNames = "host", required = true, defaultValue = "localhost") String host,
            @Option(longNames = "port", required = true) String port) {

        String backupFilePath = listDatabaseBackupFile.returnPath(Username, dataBaseName);
        return fullRestoreDatabaseService.fullRestoreDb(dataBaseName, dbType, Username, host, port, backupFilePath);
    }

    /**
     * Restores a new database using a backup from a different (old) database.
     *
     * @param dbType          The type of the database to restore.
     * @param Username        The username to use for database operations.
     * @param newdataBaseName The name of the new database to be created and
     *                        restored.
     * @param olddataBaseName The name of the old database from which the backup
     *                        will be used.
     * @return The result of the restore operation as a String.
     */
    @Command(description = "Restore a new database using a backup from a different (old) database", group = "local full restore")
    public String fullRestoreWithoutDb(
            @Option(longNames = "type", required = true, description = "The type of the database to restore") DatabaseType dbType,
            @Option(longNames = "username", required = true, description = "The username to use for database operations") String Username,
            @Option(longNames = "newdb", required = true, description = "The name of the new database to be created and restored") String newdataBaseName,
            @Option(longNames = "oldDb", required = true, description = "The name of the old database from which the backup will be used") String olddataBaseName) {
        String backupFilePath = listDatabaseBackupFile.returnPath(Username, olddataBaseName);
        return fullRestoreDatabaseService.fullRestoreWithoutDb(dbType, Username, newdataBaseName, backupFilePath);
    }

    /**
     * Drops an existing database and recreates it from a local backup file.
     *
     * <p>
     * This command deletes the specified database and restores it using a backup
     * file
     * associated with the provided username and database name. The database type
     * must
     * also be specified.
     * </p>
     *
     * @param Username     the username associated with the database backup file
     *                     (required)
     * @param dataBaseName the name of the database to drop and recreate (required)
     * @param dbType       the type of the database to restore (required)
     * @return a status message indicating the result of the operation
     */
    @Command(description = "Drop an existing database and recreate it from a local backup file", group = "local full restore")
    public String dropAndRecreateDb(
            @Option(longNames = "username", required = true, description = "The username associated with the database backup file") String Username,
            @Option(longNames = "database", required = true, description = "The name of the database to drop and recreate") String dataBaseName,
            @Option(longNames = "type", required = true, description = "The type of the database to restore") DatabaseType dbType
    ) {
        String backupFilePath = listDatabaseBackupFile.returnPath(Username, dataBaseName);
        return fullRestoreDatabaseService.dropAndRecreateDb(Username, dataBaseName, dbType, backupFilePath);
    }

    @Command(description = "Restore a PostgreSQL database using PgBackRest tool", group = "local full restore")
    public String restorePgBackRest(
            @Option(longNames = "stanza", required = true) String stanza) {
        return fullRestoreDatabaseService.retorePgbackrest(stanza);
    }

    // --------------------------------Restoration from the cloud
    // backup-----------------------------------------
    /**
     * Restores an existing database from a backup stored in the cloud.
     *
     * @param dataBaseName The name of the database to restore.
     * @param dbType The type of the database (e.g., MySQL, PostgreSQL).
     * @param Username The username used for authentication with the database.
     * @param host The host address of the database server. Defaults to "localhost".
     * @param port The port number on which the database server is running.
     * @return A status message indicating the result of the restore operation.
     */
    @Command(description = "Restore an existing database from a backup stored in the cloud", group = "cloud full restore")
    public String cloudFullRestoreDb(
            @Option(longNames = "database", required = true, description = "The name of the database to restore") String dataBaseName,
            @Option(longNames = "type", required = true, description = "The type of the database (e.g., MySQL, PostgreSQL)") DatabaseType dbType,
            @Option(longNames = "username", required = true, description = "The username used for authentication with the database") String Username,
            @Option(longNames = "host", required = true, defaultValue = "localhost", description = "The host address of the database server") String host,
            @Option(longNames = "port", required = true, description = "The port number on which the database server is running") String port) {

        String fileName = s3service.listFiles(dataBaseName, Username);
        return fullRestoreDatabaseService.cloudRestoreDb(fileName, dataBaseName, dbType, Username, host, port);
    }

}
