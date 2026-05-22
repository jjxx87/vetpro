package com.vetech.serve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("reimbursement_apportionment")
public class ReimbursementApportionment {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String reimbursementId;
    private String companyId;
    private String projectId;
    private BigDecimal percent;
    private BigDecimal amount;
}
