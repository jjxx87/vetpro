package com.vetech.serve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@TableName(value = "reimbursement_subsidy", autoResultMap = true)
public class ReimbursementSubsidy {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String reimbursementId;
    private String employeeId;
    private String startDate;
    private String endDate;
    private String startCity;
    private String endCity;
    private Integer days;
    private String mealAmount;
    private String trafficAmount;
    private String commAmount;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> calendar;
}
