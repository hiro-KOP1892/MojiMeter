package com.jobhunt.eshelper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobhunt.eshelper.config.GeminiConfig;
import com.jobhunt.eshelper.dto.AdjustRequest;
import com.jobhunt.eshelper.dto.AdjustResponse;
import com.jobhunt.eshelper.exception.GeminiApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Gemini API連携サービス
 * Gemini 1.5 Flashを使用してES文章の文字数調整提案を生成
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {
    private final GeminiConfig geminiConfig;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    /**
     * ES文章の文字数を調整するサービスメソッド
     * 
     * @param request 調整リクエスト（テキスト、目標文字数、アクション、変更量）
     * @return 調整後のレスポンス（修正案テキスト、調整後文字数）
     */
    public AdjustResponse adjustText(AdjustRequest request) {
        try {
            // リクエストの検証
            validateRequest(request);

            // Gemini API呼び出しのプロンプト構築
            String prompt = buildPrompt(request);
            log.info("Gemini APIプロンプト送信: アクション={}, 変更文字数={}", request.getAction(), request.getDiff());

            // Gemini APIを呼び出し
            String suggestedText = callGeminiApi(prompt);
            int adjustedCount = suggestedText.length();

            log.info("Gemini API応答: 修正後文字数={}", adjustedCount);

            return AdjustResponse.builder()
                    .suggestedText(suggestedText)
                    .adjustedCount(adjustedCount)
                    .success(true)
                    .build();

        } catch (GeminiApiException e) {
            log.error("Gemini API設定エラー: {}", e.getMessage());
            return AdjustResponse.builder()
                    .success(false)
                    .errorMessage("API設定エラー: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Gemini API呼び出しエラー: {}", e.getMessage(), e);
            return AdjustResponse.builder()
                    .success(false)
                    .errorMessage("文字数調整処理に失敗しました: " + e.getMessage())
                    .build();
        }
    }

    /**
     * リクエストの検証
     */
    private void validateRequest(AdjustRequest request) {
        if (request.getText() == null || request.getText().isEmpty()) {
            throw new GeminiApiException("テキストが空です");
        }
        if (request.getTargetWordCount() == null || request.getTargetWordCount() <= 0) {
            throw new GeminiApiException("目標文字数が無効です");
        }
        if (request.getDiff() == null || request.getDiff() <= 0) {
            throw new GeminiApiException("変更文字数が無効です");
        }
        if (geminiConfig.getApiKey() == null || geminiConfig.getApiKey().isEmpty() || 
            geminiConfig.getApiKey().equals("dummy-key-for-development")) {
            throw new GeminiApiException("Gemini APIキーが設定されていません。環境変数 GEMINI_API_KEY を設定してください。");
        }
    }

    /**
     * Gemini API用のプロンプトを構築
     */
    private String buildPrompt(AdjustRequest request) {
        String action = "EXPAND".equals(request.getAction()) ? "増やして" : "削って";
        return String.format(
                "以下のES（エントリーシート）文章の主旨を変えず、自然な日本語で文字数を約%d文字%sください。" +
                "修正内容のみを返してください、説明は不要です。\n\n" +
                "【元の文章】\n%s",
                request.getDiff(), action, request.getText()
        );
    }

    /**
     * Gemini APIを呼び出してテキストを調整
     */
    private String callGeminiApi(String prompt) {
        try {
            if (geminiConfig.getApiKey() == null || geminiConfig.getApiKey().isEmpty()) {
                throw new GeminiApiException("Gemini APIキーが設定されていません");
            }

            // リクエストボディを構築
            String requestBody = buildGeminiRequest(prompt);
            log.debug("Gemini APIリクエスト: {}", requestBody);

            // Gemini APIを呼び出し
            WebClient webClient = webClientBuilder.build();
            String url = GEMINI_API_URL.replace("{model}", geminiConfig.getModelName()) + 
                         "?key=" + geminiConfig.getApiKey();

            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // レスポンスをパース
            return parseGeminiResponse(response);

        } catch (Exception e) {
            log.error("Gemini API呼び出し失敗: {}", e.getMessage(), e);
            throw new GeminiApiException("Gemini API呼び出し失敗: " + e.getMessage(), e);
        }
    }

    /**
     * Gemini APIリクエストボディを構築
     */
    private String buildGeminiRequest(String prompt) throws Exception {
        String json = "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"text\": " + objectMapper.writeValueAsString(prompt) + "\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"generationConfig\": {\n" +
                "    \"temperature\": 0.7,\n" +
                "    \"maxOutputTokens\": 1024\n" +
                "  }\n" +
                "}";
        return json;
    }

    /**
     * Gemini APIレスポンスをパース
     */
    private String parseGeminiResponse(String response) throws Exception {
        log.debug("Gemini APIレスポンス: {}", response);
        
        JsonNode root = objectMapper.readTree(response);
        JsonNode candidates = root.get("candidates");
        
        if (candidates == null || candidates.isEmpty()) {
            throw new GeminiApiException("Gemini APIから有効なレスポンスが得られませんでした");
        }
        
        JsonNode content = candidates.get(0).get("content");
        if (content == null) {
            throw new GeminiApiException("APIレスポンスにcontentが含まれていません");
        }
        
        JsonNode parts = content.get("parts");
        if (parts == null || parts.isEmpty()) {
            throw new GeminiApiException("APIレスポンスにpartsが含まれていません");
        }
        
        String text = parts.get(0).get("text").asText();
        if (text == null || text.isEmpty()) {
            throw new GeminiApiException("APIレスポンスにテキストが含まれていません");
        }
        
        return text.trim();
    }
}
