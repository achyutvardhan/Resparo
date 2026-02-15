package com.resparo.dev.util;

import org.zeroturnaround.exec.ProcessExecutor;

import com.resparo.dev.domain.OperatingSystem;

public final class StopDatabase {

    public static String stop(String service) {
        OperatingSystem os = OsdetectionProvider.getOS();
        try {
            String output = "";
            switch (os) {
                case MAC -> {
                    output = new ProcessExecutor()
                            .command("brew", "services", "stop", service)
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue() == 0 ? service + "services Stopped" : service + " start failed";
                }

                case WINDOWS -> {
                    output = new ProcessExecutor()
                            .command("net", "stop", service)
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue() == 0 ? service + "services Stopped" : service + " start failed";
                }
                case LINUX -> {
                    int systemctl = new ProcessExecutor().command("which", "systemctl")
                            .redirectOutput(System.out)
                            .redirectError(System.err)
                            .execute()
                            .getExitValue();

                    if (systemctl == 0) {
                        int result = new ProcessExecutor()
                                .command("sudo", "systemctl", "stop", service)
                                .redirectOutput(System.out)
                                .redirectError(System.err)
                                .execute()
                                .getExitValue();

                        output = (result == 0)
                                ? service + " service Stopped"
                                : service + " start failed";

                    } else {

                        int result = new ProcessExecutor()
                                .command("sudo", "service", service, "stop")
                                .redirectOutput(System.out)
                                .redirectError(System.err)
                                .execute()
                                .getExitValue();

                        output = (result == 0)
                                ? service + " service Stopped"
                                : service + " start failed";
                    }
                }
                case UNKNOWN -> {
                    output = "Os not detected";
                }
            }
            return output;

        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
