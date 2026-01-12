package com.eduplatform.training.entity;

import com.eduplatform.entity.base.BaseEntity;
import com.eduplatform.entity.enums.AcademicDegree;
import com.eduplatform.entity.enums.AcademicRank;
import com.eduplatform.entity.enums.ContractType;
import com.eduplatform.entity.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TeacherProfile entity - Hồ sơ giảng viên
 * Liên kết 1-1 với User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeacherProfile extends BaseEntity {
    // Link to User
    private Integer userId;
    
    // Professional info
    private String teacherCode;         // Mã giảng viên
    private Integer facultyId;          // Đơn vị công tác (Khoa/Bộ môn)
    private Faculty faculty;
    private AcademicDegree degree;      // Học vị: Cử nhân/Thạc sĩ/Tiến sĩ
    private AcademicRank rank;          // Học hàm: PGS/GS
    
    // Contract info
    private ContractType contractType;  // Cơ hữu/Thỉnh giảng
    
    // Personal info
    private String nationalId;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String phone;
    
    // Payroll info
    private BigDecimal salaryCoefficient; // Hệ số lương
    private String bankAccount;
    private String bankName;
    private String taxCode;
}
