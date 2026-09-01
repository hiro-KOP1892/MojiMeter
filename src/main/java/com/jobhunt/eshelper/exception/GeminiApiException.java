package com.jobhunt.eshelper.exception;

/**
 * Gemini API連携時の例外
 */
public class GeminiApiException extends RuntimeException {
    public GeminiApiException(String message) {
        super(message);
    }

    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
