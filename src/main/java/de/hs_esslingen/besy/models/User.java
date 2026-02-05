package de.hs_esslingen.besy.models;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "\"user\"")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserPreferences> preferences;


}


