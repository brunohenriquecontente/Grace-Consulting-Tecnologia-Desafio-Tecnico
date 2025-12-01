package com.graceconsulting.cardmanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("\"cardNumber\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("\"password\"\\s*:\\s*\"([^\"]+)\"");
    private static final int MAX_BODY_LENGTH = 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";

        log.info("[{}] --> {} {}{} (IP: {}, User-Agent: {})",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                queryString,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            logRequestBody(requestId, requestWrapper);
            logResponseBody(requestId, responseWrapper, duration);

            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestBody(String requestId, ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            String maskedBody = maskSensitiveData(body);
            String truncatedBody = truncateBody(maskedBody);
            log.info("[{}] Request Body: {}", requestId, truncatedBody);
        }
    }

    private void logResponseBody(String requestId, ContentCachingResponseWrapper response, long duration) {
        int status = response.getStatus();
        String statusText = getStatusText(status);

        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            String maskedBody = maskSensitiveData(body);
            String truncatedBody = truncateBody(maskedBody);
            log.info("[{}] <-- {} {} ({}ms) Response Body: {}",
                    requestId, status, statusText, duration, truncatedBody);
        } else {
            log.info("[{}] <-- {} {} ({}ms)",
                    requestId, status, statusText, duration);
        }
    }

    private String maskSensitiveData(String body) {
        String masked = CARD_NUMBER_PATTERN.matcher(body)
                .replaceAll("\"cardNumber\":\"****MASKED****\"");
        masked = PASSWORD_PATTERN.matcher(masked)
                .replaceAll("\"password\":\"****MASKED****\"");
        return masked;
    }

    private String truncateBody(String body) {
        if (body.length() > MAX_BODY_LENGTH) {
            return body.substring(0, MAX_BODY_LENGTH) + "... [truncated]";
        }
        return body;
    }

    private String getStatusText(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "";
        };
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator");
    }
}
