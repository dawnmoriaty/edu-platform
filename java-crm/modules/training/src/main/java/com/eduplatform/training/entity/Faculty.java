package com.eduplatform.training.entity;

import com.eduplatform.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Faculty entity - Khoa
 * Đơn vị cấp cao nhất trong cấu trúc đào tạo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Faculty extends BaseEntity {
    private String code;        // CNTT, KT, etc.
    private String name;        // Công nghệ thông tin, Kinh tế
    private String description;
    
    private List<Major> majors;
}
