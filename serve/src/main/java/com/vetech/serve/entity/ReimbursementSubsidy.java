package com.vetech.serve.entity;


/**
 * @cn-file
 * 文件：serve/src/main/java/com/vetech/serve/entity/ReimbursementSubsidy.java
 * 说明：后端实体：领域模型/数据库映射对象
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 报销补助实体，对应补助明细表数据。
 */
@Data
@TableName(value = "reimbursement_subsidy", autoResultMap = true)
public class ReimbursementSubsidy {
    /**
     * 补助明细主键。
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
     * 出差开始日期。
     */
    private String startDate;

    /**
     * 出差结束日期。
     */
    private String endDate;

    /**
     * 出发城市编码。
     */
    private String startCity;

    /**
     * 到达城市编码。
     */
    private String endCity;

    /**
     * 补助天数。
     */
    private Integer days;

    /**
     * 餐费补助金额。
     */
    private String mealAmount;

    /**
     * 交通补助金额。
     */
    private String trafficAmount;

    /**
     * 通讯补助金额。
     */
    private String commAmount;
    
    /**
     * 每日补助日历明细，按 JSON 结构存储。
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> calendar;
}
