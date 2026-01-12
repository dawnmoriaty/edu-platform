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
 * StudentTuition entity - Công nợ học phí sinh viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudentTuition extends BaseEntity {
    private Integer studentId;
    
    private String semester;            // HK1-2024, HK2-2024
    private Integer academicYear;       // 2024
    
    private BigDecimal totalAmount;     // Tổng học phí
    private BigDecimal paidAmount;      // Đã đóng
    private BigDecimal discountAmount;  // Giảm trừ (học bổng, miễn giảm)
    private BigDecimal debtAmount;      // Còn nợ
    
    private PaymentStatus status;
    private LocalDate dueDate;          // Hạn đóng
    
    public enum PaymentStatus {
        UNPAID,         // Chưa đóng
        PARTIAL,        // Đóng 1 phần
        PAID,           // Đã đóng đủ
        OVERDUE,        // Quá hạn
        EXEMPTED        // Được miễn
    }
}
