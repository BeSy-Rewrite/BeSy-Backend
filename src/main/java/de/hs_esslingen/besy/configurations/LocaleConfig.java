package de.hs_esslingen.besy.configurations;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class LocaleConfig {

    @Value("${app.locale}")
    private String localeTag;

    private Locale buildLocale() {
        return Locale.forLanguageTag(localeTag.replace('_', '-'));
    }

    @PostConstruct
    public void setDefaultLocale() {
        Locale.setDefault(buildLocale());
    }

    @Bean
    public Locale locale() {
        return buildLocale();
    }
}
