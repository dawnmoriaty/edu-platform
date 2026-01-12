package com.eduplatform.finance.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * EnterpriseWallet entity - Ví điện tử doanh nghiệp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EnterpriseWallet extends BaseEntity {
    private Integer enterpriseId;
    
    private BigDecimal balance;         // Số dư hiện tại
    private BigDecimal totalDeposited;  // Tổng đã nạp
    private BigDecimal totalSpent;      // Tổng đã chi
}
