package com.eduplatform.training.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Major entity - Ngành học
 * Trực thuộc Khoa
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Major extends BaseEntity {
    private String code;        // KTPM, QTDNTH, etc.
    private String name;        // Kỹ thuật phần mềm, Quản trị kinh doanh
    private String description;
    
    private Integer facultyId;
    private Faculty faculty;
    
    private List<AcademicClass> classes;
}
