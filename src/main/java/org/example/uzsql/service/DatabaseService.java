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
import java.sql.SQLException;

import static org.example.uzsql.utils.DbUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseService {

    private final CreatedDatabasesRepository repository;
    private final MailService mailService;
    private final Generate generate;
    private final RecaptchaService recaptchaService;

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
    public String createDb(CreateRequest req, String userIp) {
        boolean captchaVerified = recaptchaService.verify(
                req.getRecaptchaToken(),
                userIp
        );

        if (!captchaVerified) {
            throw new IllegalArgumentException("Recaptcha tasdiqlanmadi. Qayta urinib ko‘ring.");
        }

        String dbName = generate.sanitizeIdentifier(req.getDbName());
        String username = generate.sanitizeIdentifier(req.getDbUser());
        String password = generate.generateStrongPassword();

        if (repository.existsByName(dbName)) {
            throw new DatabaseAlreadyExistsException("Database nomi allaqachon mavjud: " + dbName);
        }

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
            connection.setAutoCommit(false);
            executeUpdate(connection, "CREATE DATABASE IF NOT EXISTS `" + dbName + "`");
            String createUserSql = "CREATE USER IF NOT EXISTS '" + username + "'@'%' IDENTIFIED BY '" + password + "'";
            executeUpdate(connection, createUserSql);
            executeUpdate(connection, "GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + username + "'@'%'");
            executeUpdate(connection, "FLUSH PRIVILEGES");
            connection.commit();
            log.info("Database va user muvaffaqiyatli yaratildi");

        } catch (SQLException e) {
            rollbackQuietly(connection);
            log.error("MySQL xatosi", e);
            throw new DatabaseCreationException("Database yaratishda xatolik: " + e.getMessage(), e
            );
        } finally {
            closeQuietly(connection);
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
        } catch (Exception e) {
            log.error("Email yuborishda xatolik", e);
        }

        return saved.getEmail();
    }



}
