package com.resparo.dev.model;

import com.resparo.dev.enums.DatabaseType;

public final class JdbcUrlBuilder {
    private JdbcUrlBuilder() {
    }

    public static String builder(
            DatabaseType dbtype,
            String host,
            String port,
            String DatabaseName) {
        return switch (dbtype) {
            case MYSQL -> "jdbc:mysql://" + host + ":" + port + "/" + DatabaseName;
            case POSTGRESQL -> "jdbc:postgresql://" + host + ":" + port + "/" + DatabaseName;
            case MONGODB -> "jdbc:mongodb://" + host + ":" + port + "/" + DatabaseName;
        };
    }
}
