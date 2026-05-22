package com.vetech.serve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("reimbursement_itinerary")
public class ReimbursementItinerary {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String reimbursementId;
    private String employeeId;
    private String startCity;
    private String endCity;
    private String startDate;
    private String endDate;
    private String reason;
}
