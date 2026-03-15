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
    /**
     * Connects to a database using the provided connection parameters.
     *
     * @param host        the hostname or IP address of the database server (default: "localhost")
     * @param port        the port number on which the database server is listening
     * @param password    the password for authenticating the database user
     * @param Username    the username for authenticating the database user
     * @param dataBaseName the name of the database to connect to
     * @param dbType      the type of the database (e.g., MySQL, PostgreSQL)
     * @return            a status message indicating the result of the connection attempt
     * @throws Exception  if an error occurs while attempting to connect to the database
     */
    @Command(description = "Connect to database", group = "Database")
    public String connectDB(
            @Option(longNames = "host", required = true, defaultValue = "localhost", description = "Hostname or IP address of the database server") String host,
            @Option(longNames = "port", required = true, description = "Port number of the database server") String port,
            @Option(longNames = "password", required = true, description = "Password for the database user") String password,
            @Option(longNames = "username", required = true, description = "Username for the database user") String Username,
            @Option(longNames = "database", required = true, description = "Name of the database to connect to") String dataBaseName,
            @Option(longNames = "type", required = true, description = "Type of the database (e.g., MySQL, PostgreSQL)") DatabaseType dbType) throws Exception {
        return connectionService.connectDb(dbType, host, port, password, dataBaseName, Username);
    }
}
