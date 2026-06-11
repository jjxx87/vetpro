package com.vetech.serve.entity;


/**
 * @cn-file
 * 文件：serve/src/main/java/com/vetech/serve/entity/Reimbursement.java
 * 说明：后端实体：领域模型/数据库映射对象
 */

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * 报销单主实体，对应报销主表数据。
 */
@Data
@TableName("reimbursement")
public class Reimbursement {

    /**
     * 报销单主键。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 创建时间。
     */
    private String creationTime;

    /**
     * 报销标题。
     */
    private String reimbursementTitle;

    /**
     * 报销人 ID。
     */
    private String reimburserId;

    /**
     * 报销人工号。
     */
    private String reimburserNo;

    /**
     * 报销人姓名。
     */
    private String reimburserName;

    /**
     * 报销部门 ID。
     */
    private String reimDepartmentId;

    /**
     * 报销部门编号。
     */
    private String reimDepartmentNo;

    /**
     * 报销部门名称。
     */
    private String reimDepartmentName;

    /**
     * 费用归属公司 ID。
     */
    private String reimCompanyId;

    /**
     * 费用归属公司编号。
     */
    private String reimCompanyNo;

    /**
     * 费用归属公司名称。
     */
    private String reimCompanyName;

    /**
     * 业务类型 ID。
     */
    private String businessTypeId;

    /**
     * 业务类型编号。
     */
    private String businessTypeNo;

    /**
     * 业务类型名称。
     */
    private String businessTypeName;

    /**
     * 出差事由。
     */
    private String businessTripReason;

    /**
     * 补助总金额。
     */
    private String subsidyTotal;

    /**
     * 餐费补助金额。
     */
    private String mealAllowance;

    /**
     * 交通补助金额。
     */
    private String transportationAllowance;

    /**
     * 通讯补助金额。
     */
    private String phoneAllowance;

    /**
     * 备注信息。
     */
    private String remarks;

    /**
     * 单据状态。
     */
    private Integer status;

    /**
     * 报销关联的行程明细，仅用于业务聚合展示。
     */
    @TableField(exist = false)
    private List<ReimbursementItinerary> itineraries;

    /**
     * 报销关联的补助明细，仅用于业务聚合展示。
     */
    @TableField(exist = false)
    private List<ReimbursementSubsidy> subsidies;

    /**
     * 报销关联的费用分摊明细，仅用于业务聚合展示。
     */
    @TableField(exist = false)
    private List<ReimbursementApportionment> apportionments;

}
