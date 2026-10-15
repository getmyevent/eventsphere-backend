package com.eventsphere.modules.exhibitor.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LeadStatusConverter implements AttributeConverter<LeadStatus, String>
{
    @Override
    public String convertToDatabaseColumn(LeadStatus status) {
        if (status == null) return null;
        return status.getValue();
    }

    @Override
    public LeadStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return LeadStatus.NEW;
        return LeadStatus.fromValue(dbData);
    }
}
