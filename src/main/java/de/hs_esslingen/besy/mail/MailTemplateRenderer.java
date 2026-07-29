package de.hs_esslingen.besy.mail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;

@Component
public class MailTemplateRenderer {

    private static final String TEMPLATE_PATH = "templates/order-status-mail.html";

    // Matches conditional blocks: {{#KEY}} ... {{/KEY}} // NOSONAR
    private static final Pattern SECTION_PATTERN = Pattern.compile("\\{\\{#(\\w+)}}(.*?)\\{\\{/\\1}}", Pattern.DOTALL);

    private String template;

    @PostConstruct
    void loadTemplate() throws IOException {
        try (var in = new ClassPathResource(TEMPLATE_PATH).getInputStream()) {
            this.template = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        }
    }

    /**
     * Convenience overload without conditional sections.
     */
    public String render(Map<String, String> values) {
        return render(values, Set.of());
    }

    /**
     * Replaces all {{KEY}} placeholders with the given values (HTML-escaped).
     * Conditional blocks {{#KEY}}...{{/KEY}} are kept only if KEY is in
     * activeSections, otherwise the whole block is removed.
     */
    public String render(Map<String, String> values, Set<String> activeSections) {
        String result = resolveSections(template, activeSections);

        for (var entry : values.entrySet()) {
            String replacement = escapeHtml(entry.getValue());
            result = result.replace("{{" + entry.getKey() + "}}", replacement);
        }
        return result;
    }

    private String resolveSections(String input, Set<String> activeSections) {
        Matcher matcher = SECTION_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String body = activeSections.contains(key) ? matcher.group(2) : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(body));
        }
        matcher.appendTail(sb);
        return sb.toString();
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
