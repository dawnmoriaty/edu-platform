package com.eduplatform.career.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JobPosting entity - Tin tuyển dụng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JobPosting extends BaseEntity {
    private Integer enterpriseId;
    private EnterpriseProfile enterprise;
    
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    
    private String location;
    private String jobType;             // Full-time, Part-time, Internship
    private String experienceLevel;     // Fresher, Junior, Senior
    
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private boolean isSalaryNegotiable;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxApplications;
    
    private boolean isVip;              // Tin VIP (trả phí)
    private boolean isActive;
}
