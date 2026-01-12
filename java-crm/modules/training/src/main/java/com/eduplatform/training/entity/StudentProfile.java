package com.eduplatform.training.entity;

import com.eduplatform.entity.base.BaseEntity;
import com.eduplatform.entity.enums.Gender;
import com.eduplatform.entity.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * StudentProfile entity - Hồ sơ sinh viên
 * Liên kết 1-1 với User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudentProfile extends BaseEntity {
    // Link to User
    private Integer userId;
    
    // Academic info
    private String studentCode;     // MSSV: B21DCCN001
    private Integer classId;        // Lớp hành chính
    private AcademicClass academicClass;
    private StudentStatus status;   // Đang học/Bảo lưu/Thôi học/Tốt nghiệp
    
    // Performance
    private BigDecimal gpa;         // Điểm trung bình tích lũy
    private Integer totalCredits;   // Tổng tín chỉ tích lũy
    
    // Personal info
    private String nationalId;      // CCCD/CMND
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String phone;
    
    // Financial info
    private String bankAccount;
    private String bankName;
    private String taxCode;         // Mã số thuế cá nhân
}
