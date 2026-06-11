package com.vetech.serve.entity;


/**
 * @cn-file
 * 文件：serve/src/main/java/com/vetech/serve/entity/ReimbursementApportionment.java
 * 说明：后端实体：领域模型/数据库映射对象
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 报销费用分摊实体，对应分摊明细表数据。
 */
@Data
@TableName("reimbursement_apportionment")
public class ReimbursementApportionment {
    /**
     * 分摊明细主键。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 关联的报销单 ID。
     */
    private String reimbursementId;

    /**
     * 费用归属公司 ID。
     */
    private String companyId;

    /**
     * 项目 ID。
     */
    private String projectId;

    /**
     * 分摊比例。
     */
    private BigDecimal percent;

    /**
     * 分摊金额。
     */
    private BigDecimal amount;
}
