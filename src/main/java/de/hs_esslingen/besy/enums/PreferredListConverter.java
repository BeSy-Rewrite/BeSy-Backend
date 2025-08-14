package de.hs_esslingen.besy.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PreferredListConverter implements AttributeConverter<PreferredList, String> {
    @Override
    public String convertToDatabaseColumn(PreferredList preferredList) {
        if(preferredList == null) return null;
        return preferredList.getDescription();
    }

    @Override
    public PreferredList convertToEntityAttribute(String s) {
        if(s == null) return null;

        for (PreferredList value : PreferredList.values()) {
            if (value.getDescription().equals(s)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + s);
    }
}
