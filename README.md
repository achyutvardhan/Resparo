# Resparo - Database Backup & Restore Utility

A Spring Boot Shell-based command-line application for creating and managing database backups and restores. Resparo supports MySQL and PostgreSQL databases with multiple backup strategies (Full, Incremental, Differential) to both local and cloud storage.

**Project Reference:** [Database Backup Utility - Roadmap.sh](https://roadmap.sh/projects/database-backup-utility)

---

## 📋 Table of Contents

1. [Features](#-features)
2. [Prerequisites](#-prerequisites)
3. [Installation & Setup](#-installation--setup)
4. [Architecture](#-architecture)
5. [Command Reference](#-command-reference)
6. [Usage Examples](#-usage-examples)
7. [Project Structure](#-project-structure)
8. [Technologies](#-technologies)
9. [Configuration](#-configuration)
10. [Troubleshooting](#-troubleshooting)

---

## ✨ Features

### Database Connection

- Connect to MySQL and PostgreSQL databases
- Automatic driver installation if needed
- JDBC-based connection management with connection pooling

### Database Backup

- **Local Backups**: Store backups on the filesystem
- **Cloud Backups**: Upload backups to AWS S3
- **Backup Types**:
  - **FULL**: Complete database backup
  - **INCREMENTAL**: Backup only changes since last backup
  - **DIFFERENTIAL**: Backup changes since last full backup

### Database Restoration

- **Full Restoration**: Restore entire databases
- **Selective Restoration**: Restore specific tables or schemas
- **Point-in-Time Recovery (PITR)**: Restore to specific timestamp (PostgreSQL with PgBackRest)
- **Database Cloning**: Restore backup to a new database name
- **Drop and Recreate**: Drop existing database and restore from backup

### Advanced Features

- PostgreSQL restoration using pgbackrest tool
- Asynchronous backup operations
- Support for database auto-installation
- Cloud storage integration (AWS S3)
- Multi-database session management

---

## 🔧 Prerequisites

### System Requirements

- **Java**: Version 21 or higher
- **OS**: macOS, Linux, or Windows (with WSL)
- **Build Tool**: Maven 3.6+

### Database Servers

- **MySQL**: Version 5.7+ (optional, for MySQL operations)
- **PostgreSQL**: Version 12+ (optional, for PostgreSQL operations)
- **PgBackRest** (optional, for advanced PostgreSQL PITR)

### AWS Credentials (Optional, for Cloud Backups)

- AWS Access Key ID
- AWS Secret Access Key
- S3 Bucket access permissions

---

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/Resparo.git
cd Resparo/dev
```

### 2. Build the Project

```bash
# Using Maven
mvn clean install

# Or with the provided Maven wrapper
./mvnw clean install
```

### 3. Run the Application

```bash
# Navigate to project root
cd /Users/achyutvardhan/Resparo/dev

# Run using Maven
mvn spring-boot:run

# Or using Java directly (after build)
java -jar target/dev-0.0.1-SNAPSHOT.jar
```

### 4. Configure AWS S3 (Optional)

Create `application.properties` in `src/main/resources/`:

```properties
# AWS Configuration
aws.s3.bucket-name=your-bucket-name
aws.s3.region=us-east-1
aws.access-key-id=${AWS_ACCESS_KEY_ID}
aws.secret-access-key=${AWS_SECRET_ACCESS_KEY}
```

Or set environment variables:

```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
```

---

## 🏗️ Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Shell CLI                          │
├─────────────────────────────────────────────────────────────┤
│  Commands Layer                                             │
│  ├── DatabaseConnection (Connect to databases)             │
│  ├── DatabaseBackup (Create backups)                       │
│  ├── DatabaseFullRestore (Full restoration)                │
│  └── DatabaseSelectiveRestore (Selective restoration)      │
├─────────────────────────────────────────────────────────────┤
│  Service Layer                                              │
│  ├── DatabaseConnectionService                            │
│  ├── DatabaseBackupService                                │
│  ├── DatabaseCloudBackupService                           │
│  ├── FullRestoreDatabaseService                           │
│  ├── SelectiveRestoreDatabaseService                      │
│  └── S3service                                            │
├─────────────────────────────────────────────────────────────┤
│  Domain Models                                              │
│  ├── DatabaseType (MYSQL, POSTGRESQL)                     │
│  ├── BackupTypes (FULL, INCREMENTAL, DIFFERENTIAL)        │
│  └── OperatingSystem                                      │
├─────────────────────────────────────────────────────────────┤
│  Utility Layer                                              │
│  ├── ConnectionProvider                                   │
│  ├── JdbcUrlBuilder                                       │
│  ├── ListDatabaseBackupFile                               │
│  ├── OsdetectionProvider                                  │
│  ├── DatabseInstalltion                                   │
│  └── File/Path Utilities                                  │
├─────────────────────────────────────────────────────────────┤
│  Database Connectivity (JDBC)                               │
├─────────────────────────────────────────────────────────────┤
│  External Tools                                             │
│  ├── pg_dump (PostgreSQL)                                │
│  ├── mysql (MySQL)                                       │
│  └── pgbackrest (Advanced PostgreSQL)                   │
└─────────────────────────────────────────────────────────────┘
```

### Data Flow

**Backup Flow:**

```
User Command → DatabaseBackup → DatabaseBackupService →
System Exec (pg_dump/mysqldump) → Local Storage/S3 → Backup File
```

**Restore Flow:**

```
User Command → DatabaseFullRestore → FullRestoreDatabaseService →
System Exec (psql/mysql) → Backup File → Database Updated
```

---

## 📝 Command Reference

### Connection Commands

#### Connect to Database

```
connect-db --host <hostname> --port <port> --password <password>
           --username <username> --database <database_name>
           --type <MYSQL|POSTGRESQL>
```

**Parameters:**

- `--host`: Database server hostname (default: localhost)
- `--port`: Database server port
- `--password`: Database user password
- `--username`: Database user username
- `--database`: Database name to connect to
- `--type`: Database type (MYSQL or POSTGRESQL)

---

### Backup Commands

#### Create Local Backup

```
backup-db-local --type <MYSQL|POSTGRESQL> --backup <FULL|INCREMENTAL|DIFFERENTIAL>
                --name <database_name>
```

**Parameters:**

- `--type`: Database type (MYSQL or POSTGRESQL)
- `--backup`: Backup strategy (FULL, INCREMENTAL, or DIFFERENTIAL)
- `--name`: Database name to backup

#### Create Cloud Backup (AWS S3)

```
backup-db-cloud --type <MYSQL|POSTGRESQL> --backup <FULL|INCREMENTAL|DIFFERENTIAL>
                --name <database_name>
```

Same parameters as local backup. Requires AWS credentials configured.

---

### Restore Commands

#### Full Database Restoration (Existing Database)

```
full-restore-db --database <database_name> --type <MYSQL|POSTGRESQL>
                --username <username> --host <hostname> --port <port>
```

**Parameters:**

- `--database`: Name of existing database to restore
- `--type`: Database type
- `--username`: Database username
- `--host`: Database hostname (default: localhost)
- `--port`: Database port

#### Restore to New Database

```
full-restore-without-db --type <MYSQL|POSTGRESQL> --username <username>
                        --newdb <new_database_name> --oldDb <old_database_name>
```

Creates new database and restores from old database's backup.

#### Drop and Recreate Database

```
drop-and-recreate-db --username <username> --database <database_name>
                     --type <MYSQL|POSTGRESQL>
```

Drops existing database and recreates it from backup.

#### Selective Table Restoration

```
restore-table --database <database_name> --type <MYSQL|POSTGRESQL>
              --username <username> --tablename <table_name>
```

#### Selective Schema Restoration

```
restore-schema --database <database_name> --type <MYSQL|POSTGRESQL>
               --username <username> --schemaname <schema_name>
```

#### Point-in-Time Recovery (PostgreSQL)

```
restore-pitr-pgbackrest --stanza <stanza_name> --target-time <timestamp>
```

---

## 💡 Usage Examples

### Example 1: Complete Backup and Restore Workflow

#### Step 1: Connect to PostgreSQL Database

```bash
shell:>connect-db --host localhost --port 5432 --password mypassword \
                   --username postgres --database myapp --type POSTGRESQL
Connected to POSTGRESQL at localhost:5432
```

#### Step 2: Create Full Backup Locally

```bash
shell:>backup-db-local --type POSTGRESQL --backup FULL --name myapp
Backup created successfully: /Users/achyutvardhan/Resparo/dev/src/main/java/com/resparo/dev/storage/myapp_full_2024-01-25.sql
```

#### Step 3: Simulate Data Loss

```bash
# Drop table (data loss scenario)
DROP TABLE users;
```

#### Step 4: Restore Database

```bash
shell:>full-restore-db --database myapp --type POSTGRESQL --username postgres \
                       --host localhost --port 5432
Database restored successfully from backup
```

---

### Example 2: Cloud Backup with AWS S3

#### Step 1: Create Cloud Backup

```bash
shell:>backup-db-cloud --type MYSQL --backup FULL --name ecommerce
Backup uploaded to S3: s3://your-bucket/ecommerce_full_2024-01-25.sql
```

#### Step 2: Verify Backup in S3

```bash
aws s3 ls s3://your-bucket/ | grep ecommerce
2024-01-25 10:30:45   524288000 ecommerce_full_2024-01-25.sql
```

#### Step 3: Restore from Cloud Backup

```bash
shell:>full-restore-db --database ecommerce --type MYSQL --username root \
                       --host localhost --port 3306
Database restored successfully from S3 backup
```

---

### Example 3: Selective Table Restoration

#### Step 1: Create Full Backup

```bash
shell:>backup-db-local --type POSTGRESQL --backup FULL --name analytics
Backup created successfully
```

#### Step 2: Restore Specific Table

```bash
shell:>restore-table --database analytics --type POSTGRESQL \
                     --username postgres --tablename user_metrics
Table 'user_metrics' restored successfully
```

---

### Example 4: Incremental Backup Strategy

#### Step 1: Initial Full Backup

```bash
shell:>backup-db-local --type MYSQL --backup FULL --name production
Full backup created: /storage/production_full_2024-01-25.sql
```

#### Step 2: Daily Incremental Backup

```bash
shell:>backup-db-local --type MYSQL --backup INCREMENTAL --name production
Incremental backup created: /storage/production_incremental_2024-01-26.sql
```

#### Step 3: Weekly Differential Backup

```bash
shell:>backup-db-local --type MYSQL --backup DIFFERENTIAL --name production
Differential backup created: /storage/production_differential_2024-01-27.sql
```

---

### Example 5: Database Cloning

Clone a database to a new environment:

```bash
# Create backup from source database
shell:>backup-db-local --type POSTGRESQL --backup FULL --name source_db
Backup created successfully

# Restore to new database
shell:>full-restore-without-db --type POSTGRESQL --username postgres \
                               --newdb staging_db --oldDb source_db
Database 'staging_db' created and restored from 'source_db' backup
```

---

## 📂 Project Structure

```
Resparo/
├── README.md                                    # Main project documentation
├── DETAILED_README.md                           # This file
└── dev/                                         # Main Spring Boot application
    ├── pom.xml                                  # Maven configuration
    ├── mvnw                                     # Maven wrapper (Unix)
    ├── mvnw.cmd                                 # Maven wrapper (Windows)
    │
    ├── src/
    │   ├── main/
    │   │   ├── java/com/resparo/dev/
    │   │   │   ├── DevApplication.java          # Application entry point
    │   │   │   │
    │   │   │   ├── command/                     # Spring Shell commands
    │   │   │   │   ├── DatabaseConnection.java
    │   │   │   │   ├── DatabaseBackup.java
    │   │   │   │   ├── DatabaseFullRestore.java
    │   │   │   │   └── DatabaseSelectiveRestore.java
    │   │   │   │
    │   │   │   ├── service/                     # Business logic layer
    │   │   │   │   ├── DatabaseConnectionService.java
    │   │   │   │   ├── DatabaseBackupService.java
    │   │   │   │   ├── DatabaseCloudBackupService.java
    │   │   │   │   ├── FullRestoreDatabaseService.java
    │   │   │   │   ├── SelectiveRestoreDatabaseService.java
    │   │   │   │   └── S3service.java
    │   │   │   │
    │   │   │   ├── domain/                      # Domain models (Enums)
    │   │   │   │   ├── DatabaseType.java        # MYSQL, POSTGRESQL
    │   │   │   │   ├── BackupTypes.java         # FULL, INCREMENTAL, DIFFERENTIAL
    │   │   │   │   └── OperatingSystem.java
    │   │   │   │
    │   │   │   ├── util/                        # Utility classes
    │   │   │   │   ├── ConnectionProvider.java
    │   │   │   │   ├── JdbcUrlBuilder.java
    │   │   │   │   ├── FileNameProvider.java
    │   │   │   │   ├── ListDatabaseBackupFile.java
    │   │   │   │   ├── OsdetectionProvider.java
    │   │   │   │   ├── DatabseInstalltion.java
    │   │   │   │   ├── MysqlInstalled.java
    │   │   │   │   ├── PostgresInstalled.java
    │   │   │   │   ├── PgbackrestInstalled.java
    │   │   │   │   ├── PgbackrestBackupInstallation.java
    │   │   │   │   ├── StartDatabase.java
    │   │   │   │   ├── StopDatabase.java
    │   │   │   │   └── interfaces/
    │   │   │   │
    │   │   │   ├── config/                      # Configuration classes
    │   │   │   │   ├── ConnectionRegistry.java  # Manage active connections
    │   │   │   │   └── S3config.java           # AWS S3 configuration
    │   │   │   │
    │   │   │   └── exceptions/                  # Custom exceptions
    │   │   │
    │   │   └── resources/
    │   │       └── application.properties       # Application configuration
    │   │
    │   └── test/
    │       └── java/com/resparo/dev/
    │           └── DevApplicationTests.java     # Unit tests
    │
    └── target/                                  # Build output (generated)
        ├── classes/                            # Compiled classes
        ├── test-classes/                       # Test classes
        └── ...
```

### Key Directory Descriptions

| Directory     | Purpose                                                       |
| ------------- | ------------------------------------------------------------- |
| `command/`    | Spring Shell command definitions (@Command)                   |
| `service/`    | Business logic and database operations                        |
| `domain/`     | Enum definitions for database types and backup strategies     |
| `util/`       | Helper classes for connections, file operations, OS detection |
| `config/`     | Configuration beans and connection management                 |
| `exceptions/` | Custom exception classes                                      |

---

## 🛠️ Technologies

### Core Framework

- **Spring Boot 3.5.10**: Application framework
- **Spring Shell 3.4.1**: CLI framework for commands
- **Spring JDBC 7.0.3**: Database connectivity

### Database Drivers

- **PostgreSQL JDBC 42.7.8**: PostgreSQL connection
- **MySQL Connector 9.5.0**: MySQL connection

### Additional Libraries

- **Lombok**: Annotation-based code generation
- **AWS SDK v2 (S3)**: Cloud storage integration
- **ZT-Exec 1.12**: External process execution

### Build & Testing

- **Maven 3.6+**: Build automation
- **JUnit**: Unit testing
- **Spring Boot Test**: Integration testing

### Java Version

- **Java 21**: Latest LTS version with modern features

---

## ⚙️ Configuration

### Application Properties

Create or modify `src/main/resources/application.properties`:

```properties
# Spring Shell Configuration
spring.shell.interactive.enabled=true

# Logging
logging.level.root=INFO
logging.level.com.resparo.dev=DEBUG

# AWS S3 Configuration
aws.s3.bucket-name=resparo-backups
aws.s3.region=us-east-1

# Database backup storage directory
backup.storage.local=/Users/achyutvardhan/Resparo/dev/src/main/java/com/resparo/dev/storage

# Connection timeout (milliseconds)
database.connection.timeout=5000
```

### Environment Variables

```bash
# AWS Configuration
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1

# Database defaults (optional)
export DB_HOST=localhost
export DB_PORT=5432
```

### Database Tools Configuration

**pgbackrest.conf** (for PostgreSQL PITR):

```ini
[global]
repo1-path=/var/lib/pgbackrest
repo1-retention-full=30
log-level-console=info

[stanza-name]
pg1-path=/var/lib/postgresql/data
```

---

## 🔍 Troubleshooting

### Issue 1: PostgreSQL Connection Failed

**Error:**

```
org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```

**Solution:**

```bash
# Check if PostgreSQL is running
brew services list | grep postgres

# Start PostgreSQL
brew services start postgresql

# Verify connection
psql -U postgres -h localhost -p 5432
```

### Issue 2: MySQL Connection Error

**Error:**

```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**Solution:**

```bash
# Start MySQL service
brew services start mysql

# Test connection
mysql -u root -p -h localhost
```

### Issue 3: AWS S3 Access Denied

**Error:**

```
The AWS Access Key Id you provided does not exist in our records
```

**Solution:**

```bash
# Verify AWS credentials
aws sts get-caller-identity

# Check S3 bucket permissions
aws s3 ls s3://your-bucket-name/

# Update credentials in application.properties or environment variables
export AWS_ACCESS_KEY_ID=correct_key
export AWS_SECRET_ACCESS_KEY=correct_secret
```

### Issue 4: pg_dump Not Found

**Error:**

```
No such file or directory: pg_dump
```

**Solution:**

```bash
# Install PostgreSQL tools
brew install postgresql

# Verify installation
which pg_dump

# Add to PATH if needed
export PATH="/usr/local/opt/postgresql/bin:$PATH"
```

### Issue 5: Backup Directory Doesn't Exist

**Error:**

```
Error=2, No such file or directory [backup storage path]
```

**Solution:**

```bash
# Create backup directory
mkdir -p /Users/achyutvardhan/Resparo/dev/src/main/java/com/resparo/dev/storage

# Verify permissions
ls -la /Users/achyutvardhan/Resparo/dev/src/main/java/com/resparo/dev/storage
```

### Issue 6: Building Project Fails

**Error:**

```
[ERROR] COMPILATION ERROR
```

**Solution:**

```bash
# Clean and rebuild
mvn clean install -DskipTests

# Check Java version
java -version  # Should be 21+

# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is part of the Roadmap.sh Database Backup Utility project.

---

## 🔗 Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Shell Documentation](https://spring.io/projects/spring-shell)
- [PostgreSQL Backup Tools](https://www.postgresql.org/docs/current/backup.html)
- [MySQL Backup Guide](https://dev.mysql.com/doc/refman/8.0/en/backup-and-recovery.html)
- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)

---

## 📧 Support

For issues, questions, or contributions, please open an issue in the repository or contact the development team.

---

**Last Updated:** January 25, 2026  
**Project Version:** 0.0.1-SNAPSHOT  
**Maintained by:** Resparo Development Team
# Resparo
Database Backup Utility
https://roadmap.sh/projects/database-backup-utility
