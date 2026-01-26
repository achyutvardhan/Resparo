package com.resparo.dev.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import com.resparo.dev.domain.BackupTypes;
import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.service.DatabaseBackupService;

@Component
@Command(group = "Database", description = "Creating backup for database")
public class DatabaseBackup {
    @Autowired
    private DatabaseBackupService backupService;

    @Command(description = "Creating backup for database")
    public String backupDb(
            @Option(longNames = "type", description = "Database type can be [MYSQL , POSTGRESQL]", required = true) DatabaseType dbType,
            @Option(longNames = "backup", description = "Backup type can be [FULL , INCREMENTAL , DIFFERENTIAL]", required = true) BackupTypes backupTypes,
            @Option(longNames = "name", description = "Database Name", required = true) String DatabaseName) {
        return backupService.backupDb(backupTypes, dbType, DatabaseName);
    }
}
