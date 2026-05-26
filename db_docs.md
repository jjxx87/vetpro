# 数据库表结构文档

## 1. reimbursement 报销单主表

| 字段名称 | 字段类型 | 字段长度 | 是否必填 | 默认值 | 中文名 | 说明 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | varchar | 32 | Y | | 主键ID | |
| creation_time | varchar | 32 | N | | 创建时间 | |
| reimbursement_title | varchar | 255 | N | | 报销标题 | |
| reimburser_id | varchar | 32 | N | | 报销人ID | |
| reimburser_no | varchar | 32 | N | | 报销人工号 | |
| reimburser_name | varchar | 50 | N | | 报销人姓名 | |
| reim_department_id | varchar | 32 | N | | 报销部门ID | |
| reim_department_no | varchar | 32 | N | | 报销部门编号 | |
| reim_department_name | varchar | 50 | N | | 报销部门名称 | |
| reim_company_id | varchar | 32 | N | | 费用归属公司ID | |
| reim_company_no | varchar | 32 | N | | 费用归属公司编号 | |
| reim_company_name | varchar | 50 | N | | 费用归属公司名称 | |
| business_type_id | varchar | 32 | N | | 业务类型ID | |
| business_type_no | varchar | 32 | N | | 业务类型编号 | |
| business_type_name | varchar | 50 | N | | 业务类型名称 | |
| business_trip_reason | text | | N | | 出差事由 | |
| subsidy_total | decimal | 10,2 | N | 0.00 | 补助总金额 | |
| meal_allowance | decimal | 10,2 | N | 0.00 | 餐费补助 | |
| transportation_allowance | decimal | 10,2 | N | 0.00 | 交通补助 | |
| phone_allowance | decimal | 10,2 | N | 0.00 | 通讯补助 | |
| remarks | text | | N | | 备注信息 | |
| status | tinyint | 1 | N | 0 | 状态 | 0草稿 1已完成 2已作废 |

## 2. reimbursement_itinerary 报销单-补录行程表

| 字段名称 | 字段类型 | 字段长度 | 是否必填 | 默认值 | 中文名 | 说明 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | varchar | 32 | Y | | 主键ID | |
| reimbursement_id | varchar | 32 | Y | | 关联主表ID | |
| employee_id | varchar | 32 | N | | 出行人ID | |
| start_city | varchar | 32 | N | | 出发城市编号 | |
| end_city | varchar | 32 | N | | 到达城市编号 | |
| start_date | date | | N | | 出发日期 | |
| end_date | date | | N | | 到达日期 | |
| reason | text | | N | | 行程说明 | |

## 3. reimbursement_subsidy 报销单-补助信息表

| 字段名称 | 字段类型 | 字段长度 | 是否必填 | 默认值 | 中文名 | 说明 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | varchar | 32 | Y | | 主键ID | |
| reimbursement_id | varchar | 32 | Y | | 关联主表ID | |
| employee_id | varchar | 32 | N | | 出行人ID | |
| start_date | date | | N | | 开始日期 | |
| end_date | date | | N | | 结束日期 | |
| start_city | varchar | 32 | N | | 出发城市编号 | |
| end_city | varchar | 32 | N | | 到达城市编号 | |
| days | int | 11 | N | 0 | 补助天数 | |
| apply_amount | decimal | 10,2 | N | 0.00 | 申请金额 | |
| subsidy_amount | decimal | 10,2 | N | 0.00 | 补助金额 | |
| calendar | json | | N | | 补助日历明细JSON | |

## 4. reimbursement_apportionment 报销单-费用分摊表

| 字段名称 | 字段类型 | 字段长度 | 是否必填 | 默认值 | 中文名 | 说明 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | varchar | 32 | Y | | 主键ID | |
| reimbursement_id | varchar | 32 | Y | | 关联主表ID | |
| company_id | varchar | 32 | N | | 费用归属公司ID | |
| project_id | varchar | 32 | N | | 项目ID | |
| percent | decimal | 5,2 | N | 0.00 | 分摊比例(%) | |
| amount | decimal | 10,2 | N | 0.00 | 分摊金额 | |