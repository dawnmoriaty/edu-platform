package com.eduplatform.finance.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WalletTransaction entity - Giao dịch ví
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WalletTransaction extends BaseEntity {
    private Integer walletId;
    
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    
    private String description;
    private String referenceId;         // ID tham chiếu (tin đăng, CV xem, etc.)
    private String referenceType;       // JOB_POSTING, CV_VIEW, DEPOSIT
    
    private LocalDateTime transactionAt;
    
    public enum TransactionType {
        DEPOSIT,        // Nạp tiền
        WITHDRAW,       // Rút tiền
        POST_JOB,       // Đăng tin tuyển dụng
        POST_VIP_JOB,   // Đăng tin VIP
        VIEW_CV,        // Xem CV ứng viên
        REFUND          // Hoàn tiền
    }
}
