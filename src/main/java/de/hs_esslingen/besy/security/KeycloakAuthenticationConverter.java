package de.hs_esslingen.besy.security;


import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.convert.converter.Converter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class KeycloakAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private String clientId;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();

        Map<String, Object> resourceAccessMap = (Map<String, Object>) claims.get("resource_access");
        Map<String, Object> clientIdMap = resourceAccessMap != null ? (Map<String, Object>) resourceAccessMap.get(clientId) : null;
        Collection<String> rolesCollection = clientIdMap != null ? (Collection<String>) clientIdMap.get("roles") : Collections.emptyList();

        return rolesCollection
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }



}
