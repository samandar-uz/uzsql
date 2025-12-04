package org.example.uzsql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uzsql.dto.CreateRequest;
import org.example.uzsql.exception.DatabaseAlreadyExistsException;
import org.example.uzsql.exception.DatabaseCreationException;
import org.example.uzsql.repository.CreatedDatabasesRepository;
import org.example.uzsql.utils.CreatedDatabases;
import org.example.uzsql.utils.Generate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseService {

    private final CreatedDatabasesRepository repository;
    private final MailService mailService;
    private final Generate generate;

    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUser;

    @Value("${spring.datasource.password}")
    private String mysqlPass;

    @Value("${db.host:45.92.173.158}")
    private String dbHost;

    @Value("${db.port:3306}")
    private Integer dbPort;


    @Transactional
    public String createDb(CreateRequest req) {
        String dbName = sanitizeIdentifier(req.getDbName());
        String username = sanitizeIdentifier(req.getDbUser());
        String password = generate.generateStrongPassword();

        if (repository.existsByName(dbName)) {
            throw new DatabaseAlreadyExistsException(
                    "Database nomi allaqachon mavjud: " + dbName
            );
        }

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
            connection.setAutoCommit(false);
            executeUpdate(connection, "CREATE DATABASE IF NOT EXISTS `" + dbName + "`");
            log.info("Database yaratildi: {}", dbName);

            String createUserSql = "CREATE USER IF NOT EXISTS ?@'%' IDENTIFIED BY ?";
            try (PreparedStatement pstmt = connection.prepareStatement(createUserSql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.executeUpdate();
            }
            log.info("User yaratildi: {}", username);
            executeUpdate(connection, "GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + username + "'@'%'");
            executeUpdate(connection, "FLUSH PRIVILEGES");
            connection.commit();
            log.info("Barcha huquqlar berildi va saqlandi");

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    log.warn("Transaction rollback qilindi");
                } catch (SQLException ex) {
                    log.error("Rollback xatosi: {}", ex.getMessage());
                }
            }
            log.error("MySQL xatosi: {}", e.getMessage(), e);
            throw new DatabaseCreationException("Database yaratishda xatolik: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Connection yopishda xatolik: {}", e.getMessage());
                }
            }
        }
        CreatedDatabases saved = repository.save(
                CreatedDatabases.builder()
                        .email(req.getEmail())
                        .name(dbName)
                        .username(username)
                        .password(password)
                        .host(dbHost)
                        .port(dbPort)
                        .status("active")
                        .build()
        );

        try {
            mailService.sendCredentialsEmail(dbName, username, password, req.getEmail());
            log.info("Email yuborildi: {}", req.getEmail());
        } catch (Exception e) {
            log.error("Email yuborishda xatolik: {}", e.getMessage());
        }

        return saved.getEmail();
    }

    private String sanitizeIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Identifier bo'sh bo'lishi mumkin emas");
        }
        if (!identifier.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException(
                    "Identifier faqat harflar, raqamlar va '_' dan iborat bo'lishi kerak"
            );
        }
        if (identifier.length() > 64) {
            throw new IllegalArgumentException("Identifier 64 belgidan oshmasligi kerak");
        }

        return identifier;
    }


    private void executeUpdate(Connection conn, String sql) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }

}