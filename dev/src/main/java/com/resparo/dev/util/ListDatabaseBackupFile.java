package com.resparo.dev.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jline.reader.LineReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ListDatabaseBackupFile {
    @Autowired
    @Lazy
    private LineReader lineReader;

    public String returnPath(String username, String databaseName) {
        final String directoryPath = "/Users/achyutvardhan/Resparo/dev/storage";
        List<String> backupFileName = new ArrayList<>();
        File[] files = new File(directoryPath).listFiles();
        if (files == null) {
            throw new RuntimeException("Directory not found");
        }
        for (final File fileEntry : files) {
            boolean isPresent = fileEntry.getName().contains(username + "_" + databaseName);
            if (isPresent)
                backupFileName.add(fileEntry.getName());
        }

        if (backupFileName.isEmpty()) {
            throw new RuntimeException("No backup files found for given user and database.");
        }

        
        for (int i = 0; i < backupFileName.size(); i++) {
            System.out.println(i + " " + backupFileName.get(i));
        }
        String input = this.lineReader.readLine("select your choice of dump ");
        String fullPath = this.buildPath(backupFileName.get(Integer.parseInt(input)));
        return fullPath;
    }

    public String buildPath(String fileName) {
        return "/Users/achyutvardhan/Resparo/dev/storage/" + fileName;
    }
}
