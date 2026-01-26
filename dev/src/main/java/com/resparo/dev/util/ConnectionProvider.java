package com.resparo.dev.util;

import java.net.URI;
import java.sql.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.resparo.dev.config.ConnectionRegistry;

@Component
public class ConnectionProvider {
    @Autowired
    private ConnectionRegistry registry;


    private Connection getConnection() {
        return registry.get();
    }

    public boolean isConnected() {
        return registry.isConnected();
    }

    public String getUserName() {
        try {
            Connection conn = getConnection();
            String user = conn.getMetaData().getUserName();
            return user;

        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String getHost() {
        try {
            Connection conn = getConnection();
            String url = conn.getMetaData().getURL();

            URI uri = URI.create(url.replace("jdbc:", ""));
            return uri.getHost();

        } catch (Exception e) {
            throw new RuntimeException("Port not found Exception");
        }
    }

    public int getPort() {
        try (Connection conn = getConnection()) {
            String url = conn.getMetaData().getURL();
            URI uri = URI.create(url.replace("jdbc:", ""));
            return uri.getPort();

        } catch (Exception e) {
            throw new RuntimeException("Port not found Exception");
        }
    }

}
