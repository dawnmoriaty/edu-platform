package com.eduplatform.common.constant;

/**
 * Action enum - Các hành động chuẩn cho permission
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
    
    @Override
    public String toString() {
        return code;
    }
}
