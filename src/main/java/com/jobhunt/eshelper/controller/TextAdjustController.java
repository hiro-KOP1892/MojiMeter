package com.jobhunt.eshelper.controller;

import com.jobhunt.eshelper.dto.AdjustRequest;
import com.jobhunt.eshelper.dto.AdjustResponse;
import com.jobhunt.eshelper.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * テキスト調整REST APIのコントローラー
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TextAdjustController {
    private final GeminiService geminiService;

    /**
     * ES文章の文字数をAIで調整するエンドポイント
     *
     * @param request 調整リクエスト
     * @return 調整レスポンス
     */
    @PostMapping("/adjust")
    public ResponseEntity<AdjustResponse> adjustText(@RequestBody AdjustRequest request) {
        log.info("調整リクエスト受信: action={}, diff={}", request.getAction(), request.getDiff());
        
        AdjustResponse response = geminiService.adjustText(request);
        
        log.info("調整レスポンス返信: success={}", response.getSuccess());
        return ResponseEntity.ok(response);
    }

    /**
     * ヘルスチェックエンドポイント
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ES Helper AI is running ✅");
    }
}
