# Bug 与功能修复说明（Vetech）

本文把本次改动按“问题 → 原因 → 技术实现 → 关键代码 → 验证”整理，直接贴出核心实现与代码片段（不使用文件链接）。

---

## 1) 安全：后端不再把用户数据打印到终端

### 问题
后端运行时终端会输出 MyBatis SQL 与参数，参数里可能含用户输入（例如备注、事由等），存在敏感信息泄露风险。

### 原因
MyBatis-Plus 通过 `log-impl` 使用 `org.apache.ibatis.logging.stdout.StdOutImpl` 时，会把 SQL/参数打印到 stdout。

### 技术实现
关闭 MyBatis 日志输出实现（NoLogging）。

### 关键配置代码（serve/src/main/resources/application.yml）

```yml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl
```

### 验证
```bash
mvn -DskipTests package
```

---

## 2) 首页列表：按日期由近到远排序

### 问题
首页列表不是“最近的单据在最上面”，用户需要滚动查找最新数据。

### 原因
`service.list()` 不带排序条件，数据库返回顺序不稳定或不符合业务期望。

### 技术实现
在控制器层改为 MyBatis-Plus LambdaQuery，按 `creationTime` 倒序 `orderByDesc`。

### 关键代码（serve/src/main/java/.../controller/ReimbursementController.java）

```java
@GetMapping("/list")
public List<Reimbursement> list() {
    return reimbursementService.lambdaQuery()
            .orderByDesc(Reimbursement::getCreationTime)
            .list();
}
```

### 验证
```bash
mvn -DskipTests package
```

---

## 3) 详情页：已完成/已作废禁用所有表单与按钮

### 问题
状态为“已完成/已作废”的报销单在详情页仍可编辑/提交，会造成数据被覆盖或流程不一致。

### 技术实现
前端统一计算只读态 `isReadOnly`，并在：
- 表单：使用 Element Plus 表单 `:disabled`
- 按钮/控件：逐个 `:disabled="isReadOnly"`
- 关键业务方法：增加短路返回（防止绕过 disabled 调用）

### 核心逻辑（src/views/ReimbursementDetail.vue）

只读态判断：
```js
const isReadOnly = computed(() => formData.status === 1 || formData.status === 2)
```

表单整体禁用（基础信息）：
```vue
<el-form
  :model="formData"
  :rules="rules"
  ref="formRef"
  label-width="120px"
  class="base-form"
  :disabled="isReadOnly"
>
```

按钮禁用（示例：提交/关闭）：
```vue
<div class="footer-fixed">
  <el-button @click="handleClose" class="persistent-blue-btn" :disabled="isReadOnly">关闭</el-button>
  <el-button type="primary" @click="handleSubmit" :disabled="isReadOnly">提交</el-button>
</div>
```

方法短路（示例：提交/保存草稿入口保护）：
```js
const doSubmit = (status) => {
  if (isReadOnly.value) return
  // ...原提交逻辑
}

const handleSubmit = () => {
  if (isReadOnly.value) return
  // ...原校验逻辑
}
```

---

## 4) 详情页：新增/编辑点击关闭时提示“是否保存为草稿”

### 问题
新增/编辑时点击“关闭”容易误操作导致数据丢失。

### 技术实现
关闭按钮走二次确认弹窗：
- 确认：保存草稿（调用 `doSubmit(0)`）
- 取消：不保存直接返回
- 关闭弹窗：不做任何事

### 关键代码（src/views/ReimbursementDetail.vue）

```js
const handleClose = () => {
  if (isReadOnly.value) return
  ElMessageBox.confirm('是否保存为草稿？', '提示', {
    confirmButtonText: '保存草稿',
    cancelButtonText: '不保存',
    type: 'warning',
    distinguishCancelAndClose: true
  })
    .then(() => {
      doSubmit(0)
    })
    .catch((action) => {
      if (action === 'cancel') router.back()
    })
}
```

---

## 5) 详情页：表格列宽优化（出差日期列）

### 问题
“出差日期”显示为“开始 至 结束”，默认列宽容易挤压换行。

### 技术实现
为该列加 `min-width`。

### 关键代码（src/views/ReimbursementDetail.vue）

```vue
<el-table-column label="出差日期" min-width="220">
  <template #default="{ row }">{{ row.startDate }} 至 {{ row.endDate }}</template>
</el-table-column>
```

---

## 6) 补助信息弹窗：不勾选时输入框变灰，但“标准总额”不受影响

### 问题
取消勾选后输入框禁用是对的，但“标准总额”不应该跟着变 0（标准代表规则上限/基准，不应被勾选影响）。

### 技术实现
将“标准总额”计算逻辑改为始终按每日标准合计（与 `mealSelected/trafficSelected/commSelected` 无关）。

### 关键代码（src/views/ReimbursementDetail.vue）

标准总额：始终合计标准
```js
const currentSubsidyStandardAmount = computed(() => {
  if (!currentSubsidy.value) return 0
  return currentSubsidy.value.calendar.reduce((sum, row) => {
    return sum + (row.mealStandard || 0) + (row.trafficStandard || 0) + (row.commStandard || 0)
  }, 0)
})
```

取消勾选仅影响“是否可输入/是否参与金额合计”，不改标准字段：
```js
const currentSubsidyAmount = computed(() => {
  if (!currentSubsidy.value) return 0
  return currentSubsidy.value.calendar.reduce((sum, row) => {
    return sum + (row.mealSelected ? row.mealAmount : 0)
               + (row.trafficSelected ? row.trafficAmount : 0)
               + (row.commSelected ? row.commAmount : 0)
  }, 0)
})
```

---


