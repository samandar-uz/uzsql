package org.example.uzsql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uzsql.dto.CreateDbRequest;
import org.example.uzsql.exception.DatabaseAlreadyExistsException;
import org.example.uzsql.exception.DatabaseCreationException;
import org.example.uzsql.repository.CreatedDatabasesRepository;
import org.example.uzsql.utils.CreatedDatabases;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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



    private static final int PASSWORD_LENGTH = 16;


    private String generateStrongPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%&*-_+=";

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        // Har bir kategoriyadan kamida bitta belgi
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Qolgan belgilarni random to'ldirish
        String allChars = upper + lower + digits + special;
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Belgilarni aralashtirish (shuffle)
        return shuffleString(password.toString(), random);
    }

    private String shuffleString(String input, SecureRandom random) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * Database va user yaratish
     */
    @Transactional
    public String createDb(CreateDbRequest req) {
        String dbName = sanitizeIdentifier(req.getDbName());
        String username = sanitizeIdentifier(req.getUsername());
        String password = generateStrongPassword();

        // Avval tekshiramiz
        if (repository.existsByName(dbName)) {
            throw new DatabaseAlreadyExistsException(
                    "Database '" + dbName + "' allaqachon mavjud!"
            );
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
            conn.setAutoCommit(false);

            // 1. Database yaratish
            executeUpdate(conn, "CREATE DATABASE IF NOT EXISTS `" + dbName + "`");
            log.info("Database yaratildi: {}", dbName);

            // 2. User yaratish (MySQL 8.0+ sintaksis)
            String createUserSql = "CREATE USER IF NOT EXISTS ?@'%' IDENTIFIED BY ?";
            try (PreparedStatement pstmt = conn.prepareStatement(createUserSql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.executeUpdate();
            }
            log.info("User yaratildi: {}", username);

            // 3. Privileges berish
            executeUpdate(conn,
                    "GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + username + "'@'%'");

            // 4. Flush privileges
            executeUpdate(conn, "FLUSH PRIVILEGES");

            conn.commit();
            log.info("Barcha huquqlar berildi va saqlandi");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    log.warn("Transaction rollback qilindi");
                } catch (SQLException ex) {
                    log.error("Rollback xatosi: {}", ex.getMessage());
                }
            }
            log.error("MySQL xatosi: {}", e.getMessage(), e);
            throw new DatabaseCreationException("Database yaratishda xatolik: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
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