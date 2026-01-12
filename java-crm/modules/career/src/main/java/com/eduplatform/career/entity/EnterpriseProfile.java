package com.eduplatform.career.entity;

import com.eduplatform.entity.base.BaseEntity;
import com.eduplatform.entity.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * EnterpriseProfile entity - Hồ sơ doanh nghiệp
 * Liên kết 1-1 với User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EnterpriseProfile extends BaseEntity {
    // Link to User
    private Integer userId;
    
    // Legal info (Bắt buộc xác thực)
    private String taxCode;             // Mã số thuế - quan trọng nhất
    private String legalName;           // Tên công ty trên giấy phép kinh doanh
    private VerificationStatus verificationStatus;
    private String rejectionReason;     // Lý do từ chối (nếu có)
    
    // Branding info
    private String displayName;         // Tên hiển thị: FPT Software
    private String shortName;           // Tên viết tắt
    private String logo;
    private String website;
    private String industry;            // Lĩnh vực: IT, Ngân hàng, Bán lẻ
    private String companySize;         // 10-50, 100-500, 500+
    private String address;
    private String description;
    
    // HR Contact
    private String hrContactName;
    private String hrContactEmail;
    private String hrContactPhone;
}
