package com.resparo.dev.util;

import java.io.IOException;

import org.zeroturnaround.exec.ProcessExecutor;

public final class PgbackrestInstalled{
    public static boolean checkInstallation() {
        try {
            new ProcessExecutor()
                    .command("pgbackrest", "--version")
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