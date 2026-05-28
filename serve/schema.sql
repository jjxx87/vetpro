CREATE DATABASE IF NOT EXISTS `vetech_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `vetech_db`;

DROP TABLE IF EXISTS `reimbursement`;
CREATE TABLE `reimbursement` (
  `id` varchar(32) NOT NULL COMMENT '主键 ID',
  `creation_time` varchar(32) DEFAULT NULL COMMENT '创建时间',
  `reimbursement_title` varchar(255) DEFAULT NULL COMMENT '报销标题',
  `reimburser_id` varchar(32) DEFAULT NULL COMMENT '报销人 ID',
  `reimburser_no` varchar(32) DEFAULT NULL COMMENT '报销人工号',
  `reimburser_name` varchar(50) DEFAULT NULL COMMENT '报销人姓名',
  `reim_department_id` varchar(32) DEFAULT NULL COMMENT '报销部门 ID',
  `reim_department_no` varchar(32) DEFAULT NULL COMMENT '报销部门编号',
  `reim_department_name` varchar(50) DEFAULT NULL COMMENT '报销部门名称',
  `reim_company_id` varchar(32) DEFAULT NULL COMMENT '费用归属公司 ID',
  `reim_company_no` varchar(32) DEFAULT NULL COMMENT '费用归属公司编号',
  `reim_company_name` varchar(50) DEFAULT NULL COMMENT '费用归属公司名称',
  `business_type_id` varchar(32) DEFAULT NULL COMMENT '业务类型 ID',
  `business_type_no` varchar(32) DEFAULT NULL COMMENT '业务类型编号',
  `business_type_name` varchar(50) DEFAULT NULL COMMENT '业务类型名称',
  `business_trip_reason` text DEFAULT NULL COMMENT '出差事由',
  `subsidy_total` decimal(10,2) DEFAULT '0.00' COMMENT '补助总金额',
  `meal_allowance` decimal(10,2) DEFAULT '0.00' COMMENT '餐费补助',
  `transportation_allowance` decimal(10,2) DEFAULT '0.00' COMMENT '交通补助',
  `phone_allowance` decimal(10,2) DEFAULT '0.00' COMMENT '通讯补助',
  `remarks` text DEFAULT NULL COMMENT '备注信息',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态：0草稿 1已完成 2已作废',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销单主表';

DROP TABLE IF EXISTS `reimbursement_itinerary`;
CREATE TABLE `reimbursement_itinerary` (
  `id` varchar(32) NOT NULL COMMENT '主键 ID',
  `reimbursement_id` varchar(32) NOT NULL COMMENT '关联主表 ID',
  `employee_id` varchar(32) DEFAULT NULL COMMENT '出行人 ID',
  `start_city` varchar(32) DEFAULT NULL COMMENT '出发城市编号',
  `end_city` varchar(32) DEFAULT NULL COMMENT '到达城市编号',
  `start_date` date DEFAULT NULL COMMENT '出发日期',
  `end_date` date DEFAULT NULL COMMENT '到达日期',
  `reason` text DEFAULT NULL COMMENT '行程说明',
  PRIMARY KEY (`id`),
  KEY `idx_reimbursement_id` (`reimbursement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销单-补录行程表';

DROP TABLE IF EXISTS `reimbursement_subsidy`;
CREATE TABLE `reimbursement_subsidy` (
  `id` varchar(32) NOT NULL COMMENT '主键 ID',
  `reimbursement_id` varchar(32) NOT NULL COMMENT '关联主表 ID',
  `employee_id` varchar(32) DEFAULT NULL COMMENT '出行人 ID',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `start_city` varchar(32) DEFAULT NULL COMMENT '出发城市编号',
  `end_city` varchar(32) DEFAULT NULL COMMENT '到达城市编号',
  `days` int(11) DEFAULT '0' COMMENT '补助天数',
  `meal_amount` decimal(10,2) DEFAULT '0.00' COMMENT '餐费金额',
  `traffic_amount` decimal(10,2) DEFAULT '0.00' COMMENT '交通金额',
  `comm_amount` decimal(10,2) DEFAULT '0.00' COMMENT '通讯金额',
  `apply_amount` decimal(10,2) DEFAULT '0.00' COMMENT '申请金额',
  `subsidy_amount` decimal(10,2) DEFAULT '0.00' COMMENT '补助金额',
  `calendar` json DEFAULT NULL COMMENT '补助日历明细JSON',
  PRIMARY KEY (`id`),
  KEY `idx_reimbursement_id` (`reimbursement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销单-补助信息表';

DROP TABLE IF EXISTS `reimbursement_apportionment`;
CREATE TABLE `reimbursement_apportionment` (
  `id` varchar(32) NOT NULL COMMENT '主键 ID',
  `reimbursement_id` varchar(32) NOT NULL COMMENT '关联主表 ID',
  `company_id` varchar(32) DEFAULT NULL COMMENT '费用归属公司 ID',
  `project_id` varchar(32) DEFAULT NULL COMMENT '项目 ID',
  `percent` decimal(5,2) DEFAULT '0.00' COMMENT '分摊比例(%)',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '分摊金额',
  PRIMARY KEY (`id`),
  KEY `idx_reimbursement_id` (`reimbursement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销单-费用分摊表';
