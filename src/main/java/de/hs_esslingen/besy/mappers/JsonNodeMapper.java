package de.hs_esslingen.besy.mappers;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonNodeMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object mapToObject(Map<String, Object> value) {
        return value == null ? null : objectMapper.convertValue(value, Object.class);
    }

    public Map<String, Object> mapToMap(Object value) {
        return value == null ? null : objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {
        });
    }
}
