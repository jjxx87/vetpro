package com.vetech.serve.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("reimbursement")
public class Reimbursement {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String creationTime;

    private String reimbursementTitle;

    private String reimburserId;

    private String reimburserNo;

    private String reimburserName;

    private String reimDepartmentId;

    private String reimDepartmentNo;

    private String reimDepartmentName;

    private String reimCompanyId;

    private String reimCompanyNo;

    private String reimCompanyName;

    private String businessTypeId;

    private String businessTypeNo;

    private String businessTypeName;

    private String businessTripReason;

    private String subsidyTotal;

    private String mealAllowance;

    private String transportationAllowance;

    private String phoneAllowance;

    private String remarks;

    private Integer status;

    @TableField(exist = false)
    private List<ReimbursementItinerary> itineraries;

    @TableField(exist = false)
    private List<ReimbursementSubsidy> subsidies;

    @TableField(exist = false)
    private List<ReimbursementApportionment> apportionments;

}