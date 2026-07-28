package de.hs_esslingen.besy.mail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;

@Component
public class MailTemplateRenderer {

    private static final String TEMPLATE_PATH = "templates/order-status-mail.html";
    private String template;

    @PostConstruct
    void loadTemplate() throws IOException {
        try (var in = new ClassPathResource(TEMPLATE_PATH).getInputStream()) {
            this.template = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        }
    }

    /**
     * Replaces all {{KEY}} placeholders with the given values.
     * Values are HTML-escaped to prevent broken markup / injection.
     */
    public String render(Map<String, String> values) {
        String result = template;
        for (var entry : values.entrySet()) {
            String replacement = escapeHtml(entry.getValue());
            result = result.replace("{{" + entry.getKey() + "}}", replacement);
        }
        return result;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
