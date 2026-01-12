package com.eduplatform.career.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * StudentCV entity - CV sinh viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudentCV extends BaseEntity {
    private Integer studentId;      // Link to StudentProfile
    
    private String title;           // Tiêu đề CV
    private String summary;         // Tóm tắt
    private String skills;          // Kỹ năng (JSON hoặc text)
    private String education;       // Học vấn (JSON)
    private String experience;      // Kinh nghiệm (JSON)
    private String projects;        // Dự án (JSON)
    private String certifications;  // Chứng chỉ (JSON)
    private String languages;       // Ngôn ngữ (JSON)
    private String fileUrl;         // URL file PDF (nếu upload)
    
    private boolean isPrimary;      // CV chính
    private boolean isPublic;       // Công khai cho DN xem
}
