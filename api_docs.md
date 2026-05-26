# 接口文档

## 1.1 新增/编辑报销单

- **接口调用地址**：`/api/reimbursement/save` (或者区分新增和修改)
- **方法名**：`saveReimbursement`
- **应用场景**：前端详情页填写报销单基础信息、补录行程、补助信息及分摊信息后，点击提交或保存草稿时调用此接口。
- **接口路径**：`/api/reimbursement/save`

### 接口入参：

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| id | String | N | 主键 ID，有值时为更新，无值时为新增 | |
| title | String | Y | 报销标题 | |
| employeeId | String | Y | 报销人 ID | |
| departmentId | String | Y | 报销部门 ID | |
| companyId | String | Y | 费用归属公司 ID | |
| businessTypeId | String | Y | 业务类型 ID | |
| reason | String | Y | 出差事由 | |
| status | Integer | Y | 状态：0草稿 1已完成 2已作废 | |
| remarks | String | N | 备注信息 | |
| itineraries | List\<ItineraryData> | Y | 补录行程列表 | 见下方 ItineraryData |
| subsidies | List\<SubsidyData> | Y | 补助信息列表 | 见下方 SubsidyData |
| apportionments | List\<ApportionmentData> | Y | 分摊信息列表 | 见下方 ApportionmentData |

**ItineraryData (补录行程信息)**

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| employeeId | String | Y | 出行人 ID | |
| startCity | String | Y | 出发城市编号 | |
| endCity | String | Y | 到达城市编号 | |
| startDate | String | Y | 出发日期 | yyyy-MM-dd |
| endDate | String | Y | 到达日期 | yyyy-MM-dd |
| reason | String | Y | 行程说明 | |

**SubsidyData (补助信息)**

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| employeeId | String | Y | 出行人 ID | |
| startDate | String | Y | 开始日期 | yyyy-MM-dd |
| endDate | String | Y | 结束日期 | yyyy-MM-dd |
| startCity | String | Y | 出发城市编号 | |
| endCity | String | Y | 到达城市编号 | |
| days | Integer | Y | 补助天数 | |
| applyAmount | Decimal | Y | 申请金额 | |
| subsidyAmount | Decimal | Y | 补助金额 | |
| calendar | String(JSON) | Y | 补助日历明细 | JSON字符串 |

**ApportionmentData (分摊信息)**

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| companyId | String | Y | 费用归属公司 ID | |
| projectId | String | N | 项目 ID | |
| percent | Decimal | Y | 分摊比例(%) | |
| amount | Decimal | Y | 分摊金额 | |

### 接口出参：

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| code | Integer | Y | 状态码，200代表成功 | |
| message | String | Y | 提示信息 | |
| data | String | N | 返回新增或修改后的单据ID | |


## 1.2 查询报销单详情

- **接口调用地址**：`/api/reimbursement/detail/{id}`
- **方法名**：`getReimbursementDetail`
- **应用场景**：在列表页点击某一条报销单的编辑或详情，进入详情页时，调用此接口回显完整数据。
- **接口路径**：`/api/reimbursement/detail/{id}`

### 接口入参：

路径参数：`id` (String, 必传) - 报销单主键 ID

### 接口出参：

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| code | Integer | Y | 状态码，200代表成功 | |
| message | String | Y | 提示信息 | |
| data | ReimbursementDetailBean | Y | 报销单详细信息对象 | |

**ReimbursementDetailBean**

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| id | String | Y | 主键 ID | |
| creationTime | String | Y | 创建时间 | yyyy-MM-dd HH:mm:ss |
| reimbursementTitle | String | Y | 报销标题 | |
| reimburserId | String | Y | 报销人 ID | |
| reimDepartmentId | String | Y | 报销部门 ID | |
| reimCompanyId | String | Y | 费用归属公司 ID | |
| businessTypeId | String | Y | 业务类型 ID | |
| businessTripReason | String | Y | 出差事由 | |
| status | Integer | Y | 状态 | 0草稿 1已完成 2已作废 |
| remarks | String | N | 备注信息 | |
| subsidyTotal | String | Y | 补助总金额 | |
| mealAllowance | String | Y | 餐费补助 | |
| transportationAllowance | String | Y | 交通补助 | |
| phoneAllowance | String | Y | 通讯补助 | |
| itineraries | List\<ItineraryData> | Y | 补录行程列表 | 同新增接口 |
| subsidies | List\<SubsidyData> | Y | 补助信息列表 | 同新增接口 |
| apportionments | List\<ApportionmentData> | Y | 分摊信息列表 | 同新增接口 |

## 1.3 查询报销单分页列表

- **接口调用地址**：`/api/reimbursement/list`
- **方法名**：`getReimbursementList`
- **应用场景**：在首页报销单列表展示时调用，支持条件查询与分页。
- **接口路径**：`/api/reimbursement/list`

### 接口入参：

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| current | Integer | Y | 当前页，默认1 | |
| size | Integer | Y | 每页大小，默认10 | |
| data | QueryReimbursementListData | Y | 获取列表服务传参 | 见下方 QueryReimbursementListData |

**QueryReimbursementListData**

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| reimbursementTitle | String | N | 报销单标题 | 模糊查询 |
| status | Integer | N | 单据状态 | 0草稿 1已完成 2已作废 |
| reimburserId | String | N | 报销人ID | |
| reimDepartmentId | String | N | 报销部门ID | |
| startDate | String | N | 创建时间开始 | yyyy-MM-dd |
| endDate | String | N | 创建时间结束 | yyyy-MM-dd |

### 接口出参：

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| total | Integer | Y | 总条数 | |
| pages | Integer | Y | 总页数 | |
| current | Integer | Y | 当前页 | |
| size | Integer | Y | 每页大小 | |
| records | List\<ReimbursementListBean> | Y | 报销单信息集合 | 见下方 ReimbursementListBean |

**ReimbursementListBean**

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| id | String | Y | 主键ID | |
| reimbursementTitle | String | Y | 报销标题 | |
| reimburserName | String | Y | 报销人姓名 | |
| reimburserNo | String | Y | 报销人工号 | |
| reimDepartmentName | String | Y | 报销部门名称 | |
| reimCompanyName | String | Y | 费用归属公司名称 | |
| businessTypeName | String | Y | 业务类型名称 | |
| subsidyTotal | Decimal | Y | 补助总金额 | |
| status | Integer | Y | 状态 | 0草稿 1已完成 2已作废 |
| creationTime | String | Y | 创建时间 | yyyy-MM-dd HH:mm:ss |

## 1.4 作废报销单

- **接口调用地址**：`/api/reimbursement/cancel/{id}`
- **方法名**：`cancelReimbursement`
- **应用场景**：在报销单列表中，对于草稿状态的单据，用户点击更多操作中的“作废”时调用。
- **接口路径**：`/api/reimbursement/cancel/{id}`

### 接口入参：

路径参数：`id` (String, 必传) - 报销单主键 ID

### 接口出参：

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| code | Integer | Y | 状态码，200代表成功 | |
| message | String | Y | 提示信息 | |

## 1.5 删除报销单

- **接口调用地址**：`/api/reimbursement/delete/{id}`
- **方法名**：`deleteReimbursement`
- **应用场景**：在报销单列表中，对于不需要的单据（通常是草稿或已作废），用户点击操作列的“删除”时调用。
- **接口路径**：`/api/reimbursement/delete/{id}`

### 接口入参：

路径参数：`id` (String, 必传) - 报销单主键 ID

### 接口出参：

| 字段名 | 类型 | 是否必传 | 备注 | 格式 |
| :--- | :--- | :--- | :--- | :--- |
| code | Integer | Y | 状态码，200代表成功 | |
| message | String | Y | 提示信息 | |