package org.example.uzsql.repository;

import org.example.uzsql.utils.CreatedDatabases;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatedDatabasesRepository extends JpaRepository<CreatedDatabases, Long> {
    boolean existsByName(String dbName);

}
