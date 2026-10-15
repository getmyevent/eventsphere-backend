package com.eventsphere.modules.attendee.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AttendeeCategoryConverter implements AttributeConverter<AttendeeCategory, String> {

    @Override
    public String convertToDatabaseColumn(AttendeeCategory attribute) {
        if (attribute == null) return null;
        return attribute.getValue();
    }

    @Override
    public AttendeeCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return AttendeeCategory.DELEGATE;
        return AttendeeCategory.fromValue(dbData);
    }
}
