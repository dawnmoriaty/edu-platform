package com.eduplatform.training.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AcademicClass entity - Lớp hành chính
 * Trực thuộc Ngành và gắn với một Niên khóa cụ thể
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AcademicClass extends BaseEntity {
    private String code;        // SE1801, BA2024A, etc.
    private String name;        // Lớp SE1801
    private Integer cohortYear; // Niên khóa (năm nhập học): 2024
    
    private Integer majorId;
    private Major major;
    
    // Giáo viên chủ nhiệm
    private Integer advisorId;
}
