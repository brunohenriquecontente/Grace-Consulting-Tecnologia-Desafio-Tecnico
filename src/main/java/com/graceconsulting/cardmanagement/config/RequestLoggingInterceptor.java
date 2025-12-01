package com.graceconsulting.cardmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "startTime";
    private static final String REQUEST_ID_ATTR = "requestId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        request.setAttribute(START_TIME_ATTR, startTime);
        request.setAttribute(REQUEST_ID_ATTR, requestId);

        if (handler instanceof HandlerMethod handlerMethod) {
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();

            log.info("[{}] --> {} {} | Controller: {}.{}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    controllerName,
                    methodName);
        } else {
            log.info("[{}] --> {} {}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI());
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                          Object handler, ModelAndView modelAndView) {
        // Processamento após o controller, mas antes da view
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTR);

        if (startTime != null && requestId != null) {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (ex != null) {
                log.error("[{}] <-- {} {} | Duration: {}ms | Exception: {}",
                        requestId,
                        status,
                        getStatusText(status),
                        duration,
                        ex.getMessage());
            } else {
                log.info("[{}] <-- {} {} | Duration: {}ms",
                        requestId,
                        status,
                        getStatusText(status),
                        duration);
            }
        }
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
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            default -> "";
        };
    }
}
