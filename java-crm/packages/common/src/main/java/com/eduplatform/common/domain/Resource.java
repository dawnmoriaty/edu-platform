package com.eduplatform.common.domain;

/**
 * Resource enum - Các tài nguyên trong hệ thống
 * Dùng cho phân quyền động
 */
public enum Resource {
    // Identity
    USER("USER", "Quản lý người dùng"),
    ROLE("ROLE", "Quản lý vai trò"),
    PERMISSION("PERMISSION", "Quản lý quyền hạn"),
    
    // Training
    FACULTY("FACULTY", "Quản lý khoa"),
    MAJOR("MAJOR", "Quản lý ngành"),
    CLASS("CLASS", "Quản lý lớp"),
    STUDENT("STUDENT", "Quản lý sinh viên"),
    TEACHER("TEACHER", "Quản lý giảng viên"),
    GRADE("GRADE", "Quản lý điểm"),
    
    // Career
    ENTERPRISE("ENTERPRISE", "Quản lý doanh nghiệp"),
    JOB_POSTING("JOB_POSTING", "Quản lý tin tuyển dụng"),
    CV("CV", "Quản lý CV"),
    APPLICATION("APPLICATION", "Quản lý ứng tuyển"),
    
    // Finance
    WALLET("WALLET", "Quản lý ví"),
    TUITION("TUITION", "Quản lý học phí"),
    PAYROLL("PAYROLL", "Quản lý bảng lương");

    private final String code;
    private final String name;

    Resource(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
