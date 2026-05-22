CREATE DATABASE IF NOT EXISTS `vetech_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `vetech_db`;

DROP TABLE IF EXISTS `reimbursement`;
CREATE TABLE `reimbursement` (
  `id` varchar(32) NOT NULL COMMENT '主键 ID',
  `creation_time` varchar(32) DEFAULT NULL COMMENT '创建时间',
  `reimbursement_title` varchar(32) DEFAULT NULL COMMENT '报销标题',
  `reimburser_id` varchar(32) DEFAULT NULL COMMENT '报销人 ID',
  `reimburser_no` varchar(20) DEFAULT NULL COMMENT '报销人工号',
  `reimburser_name` varchar(20) DEFAULT NULL COMMENT '报销人姓名',
  `reim_department_id` varchar(20) DEFAULT NULL COMMENT '报销部门 ID',
  `reim_department_no` varchar(20) DEFAULT NULL COMMENT '报销部门编号',
  `reim_department_name` varchar(20) DEFAULT NULL COMMENT '报销部门名称',
  `reim_company_id` varchar(20) DEFAULT NULL COMMENT '费用归属公司 ID',
  `reim_company_no` varchar(20) DEFAULT NULL COMMENT '费用归属公司编号',
  `reim_company_name` varchar(20) DEFAULT NULL COMMENT '费用归属公司名称',
  `business_type_id` varchar(20) DEFAULT NULL COMMENT '业务类型 ID',
  `business_type_no` varchar(20) DEFAULT NULL COMMENT '业务类型编号',
  `business_type_name` varchar(20) DEFAULT NULL COMMENT '业务类型名称',
  `business_trip_reason` varchar(20) DEFAULT NULL COMMENT '出差事由',
  `subsidy_total` varchar(20) DEFAULT NULL COMMENT '补助总金额',
  `meal_allowance` varchar(20) DEFAULT NULL COMMENT '餐费补助',
  `transportation_allowance` varchar(20) DEFAULT NULL COMMENT '交通补助',
  `phone_allowance` varchar(20) DEFAULT NULL COMMENT '通讯补助',
  `remarks` varchar(20) DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销单表';
