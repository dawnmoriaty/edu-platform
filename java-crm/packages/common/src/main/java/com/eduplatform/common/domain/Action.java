package com.eduplatform.common.domain;

/**
 * Action enum - Các hành động có thể thực hiện
 * Dùng cho phân quyền động
 */
public enum Action {
    VIEW("VIEW"),
    ADD("ADD"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    EXPORT("EXPORT"),
    IMPORT("IMPORT"),
    APPROVE("APPROVE");

    private final String code;

    Action(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
