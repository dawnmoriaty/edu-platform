package com.eduplatform.career.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JobApplication entity - Ứng tuyển
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JobApplication extends BaseEntity {
    private Integer studentId;
    private Integer jobPostingId;
    private Integer cvId;           // CV dùng để ứng tuyển
    
    private String coverLetter;     // Thư xin việc
    private ApplicationStatus status;
    private String note;            // Ghi chú của HR
    
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    
    public enum ApplicationStatus {
        PENDING,        // Chờ duyệt
        REVIEWING,      // Đang xem xét
        SHORTLISTED,    // Vào vòng trong
        INTERVIEWED,    // Đã phỏng vấn
        OFFERED,        // Đã offer
        ACCEPTED,       // Đã nhận việc
        REJECTED        // Từ chối
    }
}
