package com.jobhunt.eshelper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI文字数調整API用のレスポンスDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustResponse {
    /**
     * AIが生成した修正案テキスト
     */
    private String suggestedText;

    /**
     * 修正後の実際の文字数
     */
    private Integer adjustedCount;

    /**
     * 処理が成功したかどうか
     */
    private Boolean success;

    /**
     * エラーメッセージ（失敗時）
     */
    private String errorMessage;
}
