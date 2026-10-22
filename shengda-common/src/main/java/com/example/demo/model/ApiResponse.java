package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应，替代散落的 Map<String, Object>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Integer count;

    /* ---------- 静态工厂 ---------- */

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        return r;
    }

    public static ApiResponse<Void> ok() {
        ApiResponse<Void> r = new ApiResponse<>();
        r.success = true;
        return r;
    }

    public static ApiResponse<Void> okMsg(String message) {
        ApiResponse<Void> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        return r;
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> ok(T data, int count) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        r.count = count;
        return r;
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> fail(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        return r;
    }
}
