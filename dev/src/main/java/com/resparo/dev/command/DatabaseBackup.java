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
 * Command class for creating database backups.
 * <p>
 * Provides commands to create local and cloud backups for supported database types (MYSQL, POSTGRESQL).
 * Supports backup types: FULL, INCREMENTAL, DIFFERENTIAL.
 * </p>
 * <ul>
 *   <li>{@link #backupDbLocal(DatabaseType, BackupTypes, String)} - Creates a local backup of the specified database.</li>
 *   <li>{@link #backupDbCloud(DatabaseType, BackupTypes, String)} - Creates a cloud backup of the specified database.</li>
 * </ul>
 * <p>
 * This class is a Spring component and uses services for backup operations.
 * </p>
 */
@Component
@Slf4j
@Command(group = "Database", description = "Creating backup for database")
public class DatabaseBackup {
    @Autowired
    private DatabaseBackupService backupService;

    @Autowired
    private DatabaseCloudBackupService databaseCloudBackupService;
    

    
    @Command(description = "Creates a local backup of the specified database. Supports MYSQL and POSTGRESQL types. Backup types: FULL, INCREMENTAL, DIFFERENTIAL.")
    public String backupDbLocal(
            @Option(longNames = "type", description = "Database type can be [MYSQL, POSTGRESQL]", required = true) DatabaseType dbType,
            @Option(longNames = "backup", description = "Backup type can be [FULL, INCREMENTAL, DIFFERENTIAL]", required = true) BackupTypes backupTypes,
            @Option(longNames = "name", description = "Database Name", required = true) String DatabaseName) {
        return backupService.backupDb(backupTypes, dbType, DatabaseName);
    }

    @Command(description = "Creates a cloud backup of the specified database. Supports MYSQL and POSTGRESQL types. Backup types: FULL, INCREMENTAL, DIFFERENTIAL.")
    public String backupDbCloud(
            @Option(longNames = "type", description = "Database type can be [MYSQL, POSTGRESQL]", required = true) DatabaseType dbType,
            @Option(longNames = "backup", description = "Backup type can be [FULL, INCREMENTAL, DIFFERENTIAL]", required = true) BackupTypes backupTypes,
            @Option(longNames = "name", description = "Database Name", required = true) String DatabaseName) {
        log.info("reading the cmd class", dbType, backupTypes, DatabaseName);
        return databaseCloudBackupService.backupDb(backupTypes, dbType, DatabaseName);
    }
}
