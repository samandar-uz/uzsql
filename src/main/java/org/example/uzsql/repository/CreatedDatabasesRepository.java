package org.example.uzsql.repository;

import org.example.uzsql.utils.CreatedDatabases;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreatedDatabasesRepository extends JpaRepository<@NotNull CreatedDatabases, @NotNull Long> {
    boolean existsByName(String dbName);

}
