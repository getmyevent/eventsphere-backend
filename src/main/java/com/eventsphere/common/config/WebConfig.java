package com.eventsphere.common.config;

import com.eventsphere.modules.exhibitor.model.LeadStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, LeadStatus>() {
            @Override
            public LeadStatus convert(String source) {
                if (source == null || source.isBlank()) return null;
                return LeadStatus.fromValue(source);
            }
        });
    }
}
