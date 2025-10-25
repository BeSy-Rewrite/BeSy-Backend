package de.hs_esslingen.besy.security;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class KeycloakAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {


    private static String clientId;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<String> rolesCollection = getRoles(jwt);

        return rolesCollection
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }


    private static Collection<String> getRoles(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();

        Map<String, Object> resourceAccessMap = (Map<String, Object>) claims.get("resource_access");
        Map<String, Object> clientIdMap = resourceAccessMap != null ? (Map<String, Object>) resourceAccessMap.get(clientId) : null;
        return clientIdMap != null ? (Collection<String>) clientIdMap.get("roles") : Collections.emptyList();
    }


    public static Boolean hasRole(Jwt jwt, String role) {
        if(jwt == null) return false;

        Collection<String> rolesCollection = getRoles(jwt);
        return rolesCollection.contains(role);
    }

    @Value("${keycloak-client-id}")
    public void setClientId(String keycloakClientId) {
        clientId = keycloakClientId.trim();
    }

    public static String getClientId() {
        return clientId;
    }

}
