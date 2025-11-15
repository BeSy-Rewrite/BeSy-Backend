package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "keycloak_uuid", nullable = true, unique = true)
    private String keycloakUUID;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "legacy_user_name", nullable = true)
    private String legacyUserName;

    @ElementCollection(targetClass = String.class, fetch = FetchType.LAZY)
    private Set<String> orderFilterPreferences;

}