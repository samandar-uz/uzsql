package org.example.uzsql.utils;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "created_databases")
public class CreatedDatabases {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String username;

    @Column(nullable = false, length = 300)
    private String password;

    @Column(nullable = false, length = 150)
    @Builder.Default
    private String host = "45.92.173.158";


    @Column(nullable = false)
    @Builder.Default
    private Integer port = 3306;


    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "active";

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
