package com.resparo.dev.service;

import org.zeroturnaround.exec.ProcessExecutor;

import com.resparo.dev.domain.DatabaseType;
import com.resparo.dev.domain.OperatingSystem;
import com.resparo.dev.util.OsdetectionProvider;

public class InstalltionBackupService {
    
    public  InstalltionBackupService(DatabaseType dbtype) throws Exception{
        OperatingSystem os = OsdetectionProvider.getOS();
        String output;
        switch (dbtype) {
            case MYSQL->{
                switch (os) {
                    case MAC->{}
                    case WINDOWS->{}
                    case LINUX->{}
                    case UNKNOWN->{}
                }
            }
            case POSTGRESQL->{
                switch (os) {
                    case MAC->{
                        output = new ProcessExecutor()
                                    .command("brew install pgbackrest")
                                    .redirectOutput(System.out)
                                    .redirectError(System.err)
                                    .execute()
                                    .getExitValue() == 0 ? "pgbackrest installed successful" : "pgbackrest installed failed";
                            }
                    case WINDOWS->{
                        System.out.println("Manually install pgbackrest");
                        output = "Manual installation required";
                    }
                    case LINUX->{
                        output = new ProcessExecutor()
                                    .command("apt", "install","pgbackrest")
                                    .redirectOutput(System.out)
                                    .redirectError(System.err)
                                    .execute()
                                    .getExitValue() == 0 ? "pgbackrest installed successful" : "pgbackrest installed failed";
                    }
                    case UNKNOWN->{
                        System.out.println("OS not Dectected");
                        output = "OS not Dectected";
                    }
                };
            }
        };
    }

    public void afterInstalltionMannualForPgBackrest(){
        System.err.println("pgBackRest installed successfully.\n" + //
                        "Complete the following one-time setup to enable differential backups:");
    
        System.err.println("============================================================\n" + //
                        " pgBackRest Setup – Manual Steps\n" + //
                        "============================================================\n" + //
                        "\n" + //
                        "1) Verify pgBackRest installation\n" + //
                        "--------------------------------\n" + //
                        "pgbackrest --version\n" + //
                        "\n" + //
                        "\n" + //
                        "2) Find PostgreSQL data directory\n" + //
                        "--------------------------------\n" + //
                        "psql -U postgres -c \"SHOW data_directory;\"\n" + //
                        "\n" + //
                        "NOTE: Copy the path shown (you will need it below)\n" + //
                        "\n" + //
                        "\n" + //
                        "3) Create pgBackRest directories\n" + //
                        "--------------------------------\n" + //
                        "sudo mkdir -p /var/lib/pgbackrest\n" + //
                        "sudo chown postgres:postgres /var/lib/pgbackrest\n" + //
                        "sudo chmod 750 /var/lib/pgbackrest\n" + //
                        "\n" + //
                        "sudo mkdir -p /etc/pgbackrest\n" + //
                        "sudo chown postgres:postgres /etc/pgbackrest\n" + //
                        "sudo chmod 750 /etc/pgbackrest\n" + //
                        "\n" + //
                        "\n" + //
                        "4) Create pgBackRest configuration\n" + //
                        "----------------------------------\n" + //
                        "sudo nano /etc/pgbackrest/pgbackrest.conf\n" + //
                        "\n" + //
                        "Paste the following (update pg1-path):\n" + //
                        "\n" + //
                        "[global]\n" + //
                        "repo1-path=/var/lib/pgbackrest\n" + //
                        "repo1-retention-full=7\n" + //
                        "log-level-console=info\n" + //
                        "\n" + //
                        "[mydb]\n" + //
                        "pg1-path=/PATH/TO/POSTGRES/DATA/DIRECTORY\n" + //
                        "\n" + //
                        "Save and exit.\n" + //
                        "\n" + //
                        "\n" + //
                        "5) Enable WAL archiving in PostgreSQL\n" + //
                        "------------------------------------\n" + //
                        "sudo nano postgresql.conf\n" + //
                        "\n" + //
                        "Set the following values:\n" + //
                        "\n" + //
                        "archive_mode = on\n" + //
                        "archive_command = 'pgbackrest --stanza=mydb archive-push %p'\n" + //
                        "archive_timeout = 60\n" + //
                        "\n" + //
                        "Save and exit.\n" + //
                        "\n" + //
                        "\n" + //
                        "6) Restart PostgreSQL\n" + //
                        "--------------------\n" + //
                        "Linux:\n" + //
                        "sudo systemctl restart postgresql\n" + //
                        "\n" + //
                        "macOS (Homebrew):\n" + //
                        "brew services restart postgresql\n" + //
                        "\n" + //
                        "\n" + //
                        "7) Create pgBackRest stanza\n" + //
                        "---------------------------\n" + //
                        "sudo -u postgres pgbackrest --stanza=mydb stanza-create\n" + //
                        "\n" + //
                        "\n" + //
                        "8) Verify pgBackRest configuration\n" + //
                        "----------------------------------\n" + //
                        "sudo -u postgres pgbackrest --stanza=mydb check\n" + //
                        "\n" + //
                        "\n" + //
                        "9) Take first FULL backup (required)\n" + //
                        "------------------------------------\n" + //
                        "sudo -u postgres pgbackrest --stanza=mydb --type=full backup\n" + //
                        "\n" + //
                        "\n" + //
                        "10) Take DIFFERENTIAL backup\n" + //
                        "----------------------------\n" + //
                        "sudo -u postgres pgbackrest --stanza=mydb --type=diff backup\n" + //
                        "\n" + //
                        "\n" + //
                        "11) List available backups\n" + //
                        "--------------------------\n" + //
                        "sudo -u postgres pgbackrest info\n" + //
                        "\n" + //
                        "============================================================\n" + //
                        " Setup complete\n" + //
                        "============================================================\n" + //
                        "");
    }
}
