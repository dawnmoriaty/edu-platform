package com.eduplatform.finance.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TeacherPayroll entity - Bảng lương giảng viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeacherPayroll extends BaseEntity {
    private Integer teacherId;
    
    private String period;              // Kỳ lương: 2024-01, 2024-02
    private Integer month;
    private Integer year;
    
    // Salary components
    private BigDecimal baseSalary;      // Lương cơ bản
    private BigDecimal positionAllowance;   // Phụ cấp chức vụ
    private BigDecimal degreeAllowance;     // Phụ cấp học vị
    private BigDecimal rankAllowance;       // Phụ cấp học hàm
    private BigDecimal teachingHours;       // Số giờ dạy
    private BigDecimal overtimePay;         // Tiền dạy vượt giờ
    
    // Deductions
    private BigDecimal socialInsurance;     // BHXH
    private BigDecimal healthInsurance;     // BHYT
    private BigDecimal unemploymentInsurance; // BHTN
    private BigDecimal personalIncomeTax;   // Thuế TNCN
    private BigDecimal otherDeductions;     // Khấu trừ khác
    
    // Final
    private BigDecimal grossSalary;     // Tổng thu nhập
    private BigDecimal netSalary;       // Thực lĩnh
    
    private PaymentStatus status;
    private LocalDate paymentDate;
    
    public enum PaymentStatus {
        PENDING,        // Chờ duyệt
        APPROVED,       // Đã duyệt
        PAID,           // Đã chi trả
        CANCELLED       // Đã hủy
    }
}
