package com.jobhunt.eshelper.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Gemini API設定管理用クラス
 */
@Configuration
public class GeminiConfig {
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String modelName;

    public String getApiKey() {
        return apiKey;
    }

    public String getModelName() {
        return modelName;
    }

    /**
     * ObjectMapperをBean定義
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * WebClient.Builderをカスタマイズ
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com");
    }
}
