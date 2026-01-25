package com.resparo.dev.command;

import java.net.PasswordAuthentication;
import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;
import com.resparo.dev.config.ConnectionRegistry;
import com.resparo.dev.enums.DatabaseType;
import com.resparo.dev.model.JdbcUrlBuilder;

@Component
@Command(description = "Connect to you preffered database", group = "Database Connection")
public class DatabaseConnection {

    @Autowired
    private ConnectionRegistry registry;

    @Command(description = "Connect to database")
    public String ConnectDB(
            @Option(longNames = "host", required = true, defaultValue = "localhost") String host,
            @Option(longNames = "port", required = true) String port,
            @Option(longNames = "password", required = true) String password,
            @Option(longNames = "username", required = true) String Username,
            @Option(longNames = "database", required = true) String dataBaseName,
            @Option(longNames = "type", required = true) DatabaseType dbType) throws Exception {
        if (registry.isConnected()) {
            registry.close();
        }

        String jdbcUrl = JdbcUrlBuilder.builder(dbType, host, port,dataBaseName);
        Connection connection = DriverManager.getConnection(jdbcUrl, Username, password);
        registry.set(connection);
        return "Connected to " + dbType + " at " + host + ":" + port;

    }
}
