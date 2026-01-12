package com.eduplatform.common.util;

import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class HttpResponseUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private HttpResponseUtils() {
    }

    public static void writeError(HttpServletResponse response, HttpServletRequest request, ErrorCode errorCode) throws IOException {
        writeError(response, request, errorCode.getCode(), errorCode.getMessage());
    }

    public static void writeError(HttpServletResponse response, HttpServletRequest request, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<?> apiResponse = ApiResponse.error(statusCode, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    public static void writeSuccess(HttpServletResponse response, Object data) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<?> apiResponse = ApiResponse.success(data);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
