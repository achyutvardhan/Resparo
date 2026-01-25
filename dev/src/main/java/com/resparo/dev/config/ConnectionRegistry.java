package com.resparo.dev.config;

import java.sql.Connection;

import org.springframework.stereotype.Component;

@Component
public class ConnectionRegistry {
    private Connection connection;

    public void set(Connection connection) {
        this.connection = connection;
    }

    public Connection get() {
        if (connection == null) {
            throw new IllegalStateException("No active database connection");
        }
        return connection;
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void close() throws Exception {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

}
