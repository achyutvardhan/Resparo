package com.resparo.dev.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;
import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.service.DatabaseConnectionService;


@Component
@Command(description = "Connect to you preffered database", group = "Database Connection")
public class DatabaseConnection {

    @Autowired
    private DatabaseConnectionService connectionService;
    // .cnf file implementation
    @Command(description = "Connect to database" , group = "Database")
    public String connectDB(
            @Option(longNames = "host", required = true, defaultValue = "localhost") String host,
            @Option(longNames = "port", required = true) String port,
            @Option(longNames = "password", required = true) String password,
            @Option(longNames = "username", required = true) String Username,
            @Option(longNames = "database", required = true) String dataBaseName,
            @Option(longNames = "type", required = true) DatabaseType dbType) throws Exception {
        return connectionService.connectDb(dbType, host, port, password, dataBaseName, Username);
    }
}
