package com.graceconsulting.cardmanagement.dto;

import java.util.List;

public record BatchUploadResponse(
    String batchId,
    int totalProcessed,
    int successCount,
    int duplicateCount,
    int errorCount,
    List<BatchItemError> errors
) {
    public record BatchItemError(
        int lineNumber,
        String cardNumberMasked,
        String reason
    ) {}
}
