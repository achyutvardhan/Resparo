package com.resparo.dev.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import com.resparo.dev.domain.BackupTypes;
import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.service.DatabaseBackupService;
import com.resparo.dev.service.DatabaseCloudBackupService;

import lombok.extern.slf4j.Slf4j;


/**
 * DatabaseBackup is a command component that provides database backup
 * functionality.
 * It supports creating backups for MYSQL and POSTGRESQL databases in both local
 * and cloud storage.
 * Multiple backup types are supported: FULL, INCREMENTAL, and DIFFERENTIAL.
 */

@Component
@Slf4j
@Command(group = "Database", description = "Creating backup for database")
public class DatabaseBackup {
    @Autowired
    private DatabaseBackupService backupService;

    @Autowired
    private DatabaseCloudBackupService databaseCloudBackupService;

    /**
     * Creates a local backup of the specified database.
     * The backup is stored on the local filesystem and supports both MYSQL and
     * POSTGRESQL database types.
     * You can choose between FULL, INCREMENTAL, or DIFFERENTIAL backup strategies.
     *
     * @param dbType       The type of database to backup. Supported types: MYSQL,
     *                     POSTGRESQL. Required.
     * @param backupTypes  The backup strategy to use. Supported types: FULL,
     *                     INCREMENTAL, DIFFERENTIAL. Required.
     * @param DatabaseName The name of the database to backup. Required.
     * @return A status message indicating the result of the local backup operation.
     */
    @Command(description = "Creates a local backup of the specified database. Supports MYSQL and POSTGRESQL types. Backup types: FULL, INCREMENTAL, DIFFERENTIAL.")
    public String backupDbLocal(
            @Option(longNames = "type", description = "Database type can be [MYSQL, POSTGRESQL]", required = true) DatabaseType dbType,
            @Option(longNames = "backup", description = "Backup type can be [FULL, INCREMENTAL, DIFFERENTIAL]", required = true) BackupTypes backupTypes,
            @Option(longNames = "name", description = "Database Name", required = true) String DatabaseName) {
        return backupService.backupDb(backupTypes, dbType, DatabaseName);
    }

    /**
     * Creates a cloud backup of the specified database.
     * The backup is stored in cloud storage and supports both MYSQL and POSTGRESQL
     * database types.
     * You can choose between FULL, INCREMENTAL, or DIFFERENTIAL backup strategies.
     *
     * @param dbType       The type of database to backup. Supported types: MYSQL,
     *                     POSTGRESQL. Required.
     * @param backupTypes  The backup strategy to use. Supported types: FULL,
     *                     INCREMENTAL, DIFFERENTIAL. Required.
     * @param DatabaseName The name of the database to backup. Required.
     * @return A status message indicating the result of the cloud backup operation.
     */
    @Command(description = "Creates a cloud backup of the specified database. Supports MYSQL and POSTGRESQL types. Backup types: FULL, INCREMENTAL, DIFFERENTIAL.")
    public String backupDbCloud(
            @Option(longNames = "type", description = "Database type can be [MYSQL, POSTGRESQL]", required = true) DatabaseType dbType,
            @Option(longNames = "backup", description = "Backup type can be [FULL, INCREMENTAL, DIFFERENTIAL]", required = true) BackupTypes backupTypes,
            @Option(longNames = "name", description = "Database Name", required = true) String DatabaseName) {
        log.info("reading the cmd class", dbType, backupTypes, DatabaseName);
        return databaseCloudBackupService.backupDb(backupTypes, dbType, DatabaseName);
    }
}
