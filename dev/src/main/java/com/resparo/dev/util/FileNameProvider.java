package com.resparo.dev.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.resparo.dev.domain.DatabaseType;

public final class FileNameProvider {

    public static Path provideFileName(String user, DatabaseType dbtype, String databaseName) {
            String baseDir = "/Users/achyutvardhan/Resparo/dev/storage";
            new File(baseDir).mkdirs();
            return switch (dbtype) {
                case POSTGRESQL -> {
                    String fileName = user+"_"+databaseName + "_" + System.currentTimeMillis() + ".dump";
                    yield Paths.get(baseDir, fileName);
                }
                case MYSQL -> {
                     String fileName = user+"_"+databaseName + "_" + System.currentTimeMillis() + ".sql";
                    yield Paths.get(baseDir, fileName);
                }
            };
    }
}
