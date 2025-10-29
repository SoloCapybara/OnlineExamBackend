package com.dxd.onlineexam.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 配置 LocalDateTime 的序列化格式（返回给前端）
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        
        // 配置 LocalDateTime 的反序列化（接收前端数据）
        // 支持多种格式
        javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String dateString = p.getText();
                
                // 尝试多种格式
                DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME,  // 2025-10-30T10:00:00
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),  // 2025-10-30 10:00:00
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")  // 2025-10-30T10:00
                };
                
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDateTime.parse(dateString, formatter);
                    } catch (DateTimeParseException e) {
                        // 继续尝试下一个格式
                    }
                }
                
                throw new IOException("无法解析日期时间: " + dateString);
            }
        });
        
        return Jackson2ObjectMapperBuilder.json()
                .modules(javaTimeModule)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}

