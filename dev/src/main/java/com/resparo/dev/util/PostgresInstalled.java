package com.resparo.dev.util;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.ProcessExecutor;

import com.resparo.dev.util.interfaces.DatabaseInstallation;

@Component
public class PostgresInstalled implements DatabaseInstallation {
    @Override
    public  boolean checkInstallation() {
        try {
            new ProcessExecutor()
                    .command("psql", "--version")
                    .readOutput(true)
                    .execute();

            return true;

        } catch (IOException e) {
            // OS cannot find binary
            return false;

        } catch (Exception e) {
            // Installed, but misconfigured or permission issue
            return true;
        }
    }
}
