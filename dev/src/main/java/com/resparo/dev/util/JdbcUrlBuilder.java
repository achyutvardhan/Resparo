package com.resparo.dev.util;

import com.resparo.dev.domain.DatabaseType;

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
            // case MONGODB -> "jdbc:mongodb://" + host + ":" + port + "/" + DatabaseName;
        };
    }
}
