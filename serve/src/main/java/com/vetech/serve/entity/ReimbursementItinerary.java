package com.vetech.serve.entity;


/**
 * @cn-file
 * 文件：serve/src/main/java/com/vetech/serve/entity/ReimbursementItinerary.java
 * 说明：后端实体：领域模型/数据库映射对象
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 报销行程实体，对应补录行程明细表数据。
 */
@Data
@TableName("reimbursement_itinerary")
public class ReimbursementItinerary {
    /**
     * 行程明细主键。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 关联的报销单 ID。
     */
    private String reimbursementId;

    /**
     * 出行人员 ID。
     */
    private String employeeId;

    /**
     * 出发城市编码。
     */
    private String startCity;

    /**
     * 到达城市编码。
     */
    private String endCity;

    /**
     * 出发日期。
     */
    private String startDate;

    /**
     * 到达日期。
     */
    private String endDate;

    /**
     * 行程说明。
     */
    private String reason;
}
