package com.graceconsulting.cardmanagement.dto;

public record BatchUploadResponse(
    String batchId,
    int totalProcessed,
    int successCount,
    int errorCount
) {}
