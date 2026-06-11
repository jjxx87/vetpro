package com.vetech.serve.mapper;


/**
 * @cn-file
 * 文件：serve/src/main/java/com/vetech/serve/mapper/ReimbursementApportionmentMapper.java
 * 说明：后端 Mapper：数据库访问层（MyBatis）
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vetech.serve.entity.ReimbursementApportionment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报销分摊 Mapper，负责分摊明细数据的数据库访问。
 */
@Mapper
public interface ReimbursementApportionmentMapper extends BaseMapper<ReimbursementApportionment> {
}
