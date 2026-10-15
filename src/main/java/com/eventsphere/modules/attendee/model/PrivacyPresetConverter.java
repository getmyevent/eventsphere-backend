package com.eventsphere.modules.attendee.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PrivacyPresetConverter implements AttributeConverter<PrivacyPreset, String> {

    @Override
    public String convertToDatabaseColumn(PrivacyPreset preset) {
        if (preset == null) return null;
        return preset.getValue();
    }

    @Override
    public PrivacyPreset convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return PrivacyPreset.CONNECTIONS_ONLY;
        return PrivacyPreset.fromValue(dbData);
    }
}
