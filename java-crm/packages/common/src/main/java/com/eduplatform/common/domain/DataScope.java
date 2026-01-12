package com.eduplatform.common.domain;

/**
 * DataScope - Phạm vi dữ liệu
 * 1: All - Tất cả
 * 2: Department - Phòng ban
 * 3: Own - Chỉ của mình
 */
public enum DataScope {
    ALL(1, "Tất cả"),
    DEPARTMENT(2, "Phòng ban"),
    OWN(3, "Cá nhân");

    private final int code;
    private final String name;

    DataScope(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static DataScope fromCode(int code) {
        for (DataScope scope : values()) {
            if (scope.code == code) {
                return scope;
            }
        }
        return OWN;
    }
}
