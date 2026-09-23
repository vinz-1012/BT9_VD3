package vn.iotstar.config;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DatabaseInitializer {

    public static void main(String[] args) {
        initDatabase();
    }

    public static void initDatabase() {
        try {
            Properties props = new Properties();

            File envFile = new File(".env");
            if (envFile.exists()) {
                try (FileInputStream fis = new FileInputStream(envFile)) {
                    props.load(fis);
                }
            }

            File appPropsFile = new File("src/main/resources/application.properties");
            if (appPropsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(appPropsFile)) {
                    props.load(fis);
                }
            }

            String url = props.getProperty("spring.datasource.url", "jdbc:sqlserver://localhost:1433;databaseName=webst3;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=true");
            String username = props.getProperty("spring.datasource.username", props.getProperty("DB_USERNAME", "sa"));
            String password = props.getProperty("spring.datasource.password", props.getProperty("DB_PASSWORD", "123456"));

            String dbName = "webst3";
            Pattern dbNamePattern = Pattern.compile("databaseName=([^;]+)");
            Matcher matcher = dbNamePattern.matcher(url);
            if (matcher.find()) {
                dbName = matcher.group(1);
            }

            String masterUrl = url.replaceAll("databaseName=[^;]+", "databaseName=master");
            if (!masterUrl.contains("databaseName=master")) {
                masterUrl = masterUrl + ";databaseName=master";
            }

            log.info("Checking if database [{}] exists in SQL Server...", dbName);

            try (Connection conn = DriverManager.getConnection(masterUrl, username, password);
                 Statement stmt = conn.createStatement()) {

                String checkSql = "SELECT name FROM sys.databases WHERE name = N'" + dbName + "'";
                try (ResultSet rs = stmt.executeQuery(checkSql)) {
                    if (!rs.next()) {
                        log.info("Database [{}] does not exist. Creating database automatically...", dbName);
                        String createSql = "CREATE DATABASE [" + dbName + "]";
                        stmt.executeUpdate(createSql);
                        log.info("Database [{}] created successfully in SQL Server!", dbName);
                    } else {
                        log.info("Database [{}] already exists in SQL Server.", dbName);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not automatically create SQL Server database (it may already exist or connection failed): {}", e.getMessage());
        }
    }
}
