package com.resparo.dev.util;

import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;

import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.domain.OperatingSystem;



public class DatabseInstalltion{

    public DatabseInstalltion(DatabaseType dbtype) {
        try {
            OperatingSystem os = OsdetectionProvider.getOS();
            switch (dbtype) {
                case MYSQL -> {
                    switch (os) {
                        case MAC -> {
                            new ProcessExecutor()
                                    .command("brew", "install", "mysql")
                                    .redirectOutput(System.out)
                                    .redirectError(System.err)
                                    .execute();
                            System.out.println("mysql installed successful");
                            // new ProcessExecutor()
                            // .command("brew", "services", "start", "mysql")
                            // .redirectOutput(System.out)
                            // .redirectError(System.err)
                            // .execute();
                        }
                        case WINDOWS -> {
                            System.out.println("Manually install mysql");
                        }
                        case LINUX -> {
                            new ProcessExecutor()
                                    .command("sudo", "apt", "update")
                                    .redirectOutput(System.out)
                                    .redirectError(System.err)
                                    .execute();

                            new ProcessExecutor()
                                    .command("sudo", "apt", "install", "-y", "mysql-server")
                                    .redirectOutput(System.out)
                                    .redirectError(System.err)
                                    .execute();
                            System.out.println("mysql installed successful");
                            // new ProcessExecutor()
                            // .command("sudo", "systemctl", "start", "mysql")
                            // .redirectOutput(System.out)
                            // .redirectError(System.err)
                            // .execute();
                            // new ProcessExecutor()
                            // .command("sudo", "systemctl", "enable", "mysql")
                            // .redirectOutput(System.out)
                            // .redirectError(System.err)
                            // .execute();
                            // new ProcessExecutor()
                            // .command("sudo", "systemctl", "status", "mysql")
                            // .redirectOutput(System.out)
                            // .redirectError(System.err)
                            // .execute();
                        }
                        case UNKNOWN -> {
                            System.out.println("OS not Dectected");
                        }
                    }
                }
                case POSTGRESQL -> {
                    switch (os) {
                        case MAC -> {
                            new ProcessExecutor()
                                    .command("brew", "install", "postgresql")
                                    .redirectOutput(System.out)
                                    .redirectError(System.err)
                                    .execute();
                            System.out.println("postgresql installed successful");
                        }
                        case WINDOWS -> {
                            System.out.println("Manually install pgbackrest");
                        }
                        case LINUX -> {
                            new ProcessExecutor()
                                    .command("sudo", "apt", "update")
                                    .redirectOutput(System.out)
                                    .redirectError(System.err)
                                    .execute();

                            new ProcessExecutor()
                                    .command("sudo", "apt", "install", "-y",
                                            "postgresql", "postgresql-contrib")
                                    .redirectOutput(System.out)
                                    .redirectError(System.err)
                                    .execute();

                            System.out.println("pgbackrest installed successful");
                        }
                        case UNKNOWN -> {
                            System.out.println("OS not Dectected");
                        }
                    }
                    ;
                }
            }
            ;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}
