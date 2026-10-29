package com.taller.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    public static final String INSTANT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    @Bean
    public JsonMapperBuilderCustomizer instantFormatCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("instant-iso-format");
            module.addSerializer(Instant.class, new InstantIsoSerializer());
            builder.addModule(module);
        };
    }

    private static final class InstantIsoSerializer extends ValueSerializer<Instant> {

        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern(INSTANT_PATTERN).withZone(ZoneOffset.UTC);

        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt)
                throws JacksonException {
            gen.writeString(FORMATTER.format(value));
        }
    }
}
