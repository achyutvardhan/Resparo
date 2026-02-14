package com.resparo.dev.service;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.resparo.dev.config.ConnectionRegistry;
import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.util.JdbcUrlBuilder;
import com.resparo.dev.util.MysqlInstalled;
import com.resparo.dev.util.PostgresInstalled;

@Service
public class DatabaseConnectionService {
    @Autowired
    private ConnectionRegistry registry;
    @Autowired
    private MysqlInstalled mysqlInstalled;
    @Autowired
    private PostgresInstalled postgresInstalled;

    public String connectDb(DatabaseType dbType, String host, String port, String password, String dataBaseName,
            String username) {

        try {
            if (registry.isConnected()) {
                registry.close();
            }
            if (dbType == DatabaseType.MYSQL && !mysqlInstalled.checkInstallation()
                    || dbType == DatabaseType.POSTGRESQL && !postgresInstalled.checkInstallation())
                new DatabseInstalltionService(dbType);

            String jdbcUrl = JdbcUrlBuilder.builder(dbType, host, port, dataBaseName);
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            registry.set(connection);
            return "Connected to " + dbType + " at " + host + ":" + port;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
