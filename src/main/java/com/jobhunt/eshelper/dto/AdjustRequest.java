package com.jobhunt.eshelper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI文字数調整API用のリクエストDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustRequest {
    /**
     * 調整対象のテキスト
     */
    private String text;

    /**
     * 目標文字数（例：400）
     */
    private Integer targetWordCount;

    /**
     * 調整アクション（"EXPAND" または "SHRINK"）
     */
    private String action;

    /**
     * 変更する文字数（例：30、20）
     */
    private Integer diff;
}
