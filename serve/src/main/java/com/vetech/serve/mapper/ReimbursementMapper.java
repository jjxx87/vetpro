package com.vetech.serve.mapper;


/**
 * @cn-file
 * 文件：serve/src/main/java/com/vetech/serve/mapper/ReimbursementMapper.java
 * 说明：后端 Mapper：数据库访问层（MyBatis）
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vetech.serve.entity.Reimbursement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报销主表 Mapper，负责报销单主数据的数据库访问。
 */
@Mapper
public interface ReimbursementMapper extends BaseMapper<Reimbursement> {
}
