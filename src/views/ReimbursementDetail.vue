<template>
  <div class="detail-container">
    <!-- Header -->
    <div class="header-fixed">
      <div class="header-title">差旅费用报销单</div>
      <div class="header-date">提单日期: {{ formData.createTime || currentDate }}</div>
    </div>

    <div class="form-content">
      <el-collapse v-model="activeNames">
        <!-- 基础信息 -->
        <el-collapse-item name="1">
          <template #title>
            <div class="collapse-title-custom">基础信息</div>
          </template>
          <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px" class="base-form">
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="报销标题" prop="title">
                  <el-input v-model="formData.title" maxlength="500" placeholder="请输入报销标题" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="报销人" prop="employeeId">
                  <el-select v-model="formData.employeeId" placeholder="请选择" style="width: 100%">
                    <el-option v-for="item in dictStore.employees" :key="item.reimburserId" :label="item.reimburserName" :value="item.reimburserId" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="报销部门" prop="departmentId">
                  <el-select v-model="formData.departmentId" placeholder="请选择" style="width: 100%">
                    <el-option v-for="item in dictStore.departments" :key="item.reimDepartmentId" :label="item.reimDepartmentName" :value="item.reimDepartmentId" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="费用归属公司" prop="companyId">
                  <el-select v-model="formData.companyId" placeholder="请选择" style="width: 100%">
                    <el-option v-for="item in dictStore.companies" :key="item.reimCompanyId" :label="item.reimCompanyName" :value="item.reimCompanyId" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="业务类型" prop="businessTypeId">
                  <el-tree-select v-model="formData.businessTypeId" :data="dictStore.businessTypeTree" placeholder="请选择" check-strictly style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="出差事由" prop="reason">
                  <el-input type="textarea" v-model="formData.reason" maxlength="500" show-word-limit placeholder="请输入" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-collapse-item>

        <!-- 补录行程 -->
        <el-collapse-item name="2">
          <template #title>
            <div class="collapse-title-custom">
              <span>补录行程</span>
              <el-button type="primary" link @click.stop="openItineraryDialog()">⊕ 补录行程</el-button>
            </div>
          </template>
          <el-table :data="formData.itineraries" style="width: 100%">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column label="出行人员">
              <template #default="{ row }">{{ getEmployeeDisplay(row.employeeId) }}</template>
            </el-table-column>
            <el-table-column label="出差日期">
              <template #default="{ row }">{{ row.startDate }} 至 {{ row.endDate }}</template>
            </el-table-column>
            <el-table-column label="行程">
              <template #default="{ row }">{{ getCityName(row.startCity) }} - {{ getCityName(row.endCity) }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="行程说明" show-overflow-tooltip />
            <el-table-column label="操作" width="150">
              <template #default="{ row, $index }">
                <el-button type="primary" link @click="openItineraryDialog(row, $index)">编辑</el-button>
                <el-button type="primary" link @click="copyItinerary(row)">复制</el-button>
                <el-button type="danger" link @click="deleteItinerary($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>

        <!-- 补助信息 -->
        <el-collapse-item name="3">
          <template #title>
            <div class="collapse-title-custom">
              <span>补助信息</span>
              <span class="subsidy-total-hint">{{ totalSubsidy.toFixed(2) }} (补助天数: {{ totalDays }}天)</span>
            </div>
          </template>
          <el-alert title="1、请根据实际出差日期选择补助 2、出差期间当日有用餐安排的请自行核减当日餐补 3、出差期间当日有用车的，请自行核减当日交补" type="warning" :closable="false" style="margin-bottom: 10px;" />
          <el-table :data="formData.subsidies" style="width: 100%">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column label="出行人">
              <template #default="{ row }">{{ getEmployeeDisplay(row.employeeId) }}</template>
            </el-table-column>
            <el-table-column label="出差日期">
              <template #default="{ row }">{{ row.startDate }} 至 {{ row.endDate }}</template>
            </el-table-column>
            <el-table-column prop="days" label="补助天数" />
            <el-table-column label="行程">
              <template #default="{ row }">{{ getCityName(row.startCity) }} - {{ getCityName(row.endCity) }}</template>
            </el-table-column>
            <el-table-column label="补助城市">
              <template #default="{ row }">{{ getCityName(row.endCity) }}</template>
            </el-table-column>
            <el-table-column label="申请金额" width="120" align="right">
              <template #default="{ row }">
                {{ row.applyAmount ? row.applyAmount.toFixed(2) : '0.00' }}
              </template>
            </el-table-column>
            <el-table-column label="补助金额" width="120" align="right">
              <template #default="{ row }">
                {{ row.subsidyAmount ? row.subsidyAmount.toFixed(2) : '0.00' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row, $index }">
                <el-button type="primary" link @click="openSubsidyDialog(row, $index)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>

        <!-- 费用合计 -->
        <el-collapse-item name="4">
          <template #title>
            <div class="collapse-title-custom">费用合计</div>
          </template>
          <el-row :gutter="20" class="fee-total-row">
            <el-col :span="6">补助总金额: {{ totalSubsidy.toFixed(2) }}</el-col>
            <el-col :span="6">餐费补助: {{ totalMeal.toFixed(2) }}</el-col>
            <el-col :span="6">交通补助: {{ totalTraffic.toFixed(2) }}</el-col>
            <el-col :span="6">通讯补助: {{ totalComm.toFixed(2) }}</el-col>
          </el-row>
        </el-collapse-item>

        <!-- 费用归属及分摊 -->
        <el-collapse-item name="5">
          <template #title>
            <div class="collapse-title-custom">
              <span>费用归属及分摊</span>
              <span class="subsidy-total-hint">(分摊金额: {{ totalSubsidy.toFixed(2) }})</span>
            </div>
          </template>
          <el-table :data="formData.apportionments" style="width: 100%">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column label="费用归属*">
              <template #default="{ row, $index }">
                <el-select v-model="row.companyId" placeholder="请选择" style="width: 100%" :disabled="$index === 0 && false">
                  <el-option v-for="item in dictStore.companies" :key="item.reimCompanyId" :label="item.reimCompanyName" :value="item.reimCompanyId" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="项目">
              <template #default="{ row }">
                <el-select v-model="row.projectId" placeholder="请选择" style="width: 100%" clearable>
                  <el-option v-for="item in dictStore.projects" :key="item.projectId" :label="item.projectName" :value="item.projectId" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="分摊比例(%)*">
              <template #header>
                <span>分摊比例 <el-button type="primary" link @click="evenApportion">均摊</el-button></span>
              </template>
              <template #default="{ row, $index }">
                <el-input-number v-model="row.percent" :min="0" :max="100" :precision="2" :controls="false" :disabled="$index === 0" @change="handleApportionPercentChange($index)" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="分摊金额*">
              <template #default="{ row }">
                <el-input-number v-model="row.amount" :disabled="true" :precision="2" :controls="false" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button type="danger" link @click="deleteApportionment($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="text-align: center; margin-top: 10px;">
            <el-button type="primary" link @click="addApportionment">⊕ 添加一行</el-button>
          </div>
          <div class="apportion-footer">
            <span>合计: 100.00%</span>
            <span>CNY {{ totalSubsidy.toFixed(2) }}</span>
          </div>
        </el-collapse-item>

        <!-- 备注信息 -->
        <el-collapse-item name="6">
          <template #title>
            <div class="collapse-title-custom">
              <span>备注信息</span>
              <el-button type="danger" link @click.stop="clearRemarks">删除备注</el-button>
            </div>
          </template>
          <el-input type="textarea" v-model="formData.remarks" maxlength="1000" show-word-limit placeholder="请输入备注信息" :rows="4" />
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- Footer Buttons -->
    <div class="footer-fixed">
      <el-button @click="handleClose">关闭</el-button>
      <el-button @click="handleSaveDraft" :disabled="formData.status === 1 || formData.status === 2">保存草稿</el-button>
      <el-button type="primary" @click="handleSubmit" :disabled="formData.status === 1 || formData.status === 2">提交</el-button>
    </div>

    <!-- 补录行程弹窗 -->
    <el-dialog v-model="itineraryVisible" title="补录行程" width="800px" destroy-on-close>
      <el-alert title="仅可补录未从申请单带入或未产生费用的行程信息。跨天跨城行程填写说明： 出发城市-到达城市：武汉-北京; 出发日期-到达日期：1号-5号; 1号~5号补助按北京匹配;" type="info" :closable="false" style="margin-bottom: 15px;" />
      <el-form :model="itineraryForm" :rules="itineraryRules" ref="itineraryFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出行人" prop="employeeId">
              <el-select v-model="itineraryForm.employeeId" placeholder="请选择" style="width: 100%">
                <el-option v-for="item in dictStore.employees" :key="item.reimburserId" :label="item.reimburserName" :value="item.reimburserId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出发城市" prop="startCity">
              <el-select v-model="itineraryForm.startCity" placeholder="请选择" style="width: 100%">
                <el-option v-for="item in dictStore.cities" :key="item.cityNo" :label="item.cityName" :value="item.cityNo" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到达城市" prop="endCity">
              <el-select v-model="itineraryForm.endCity" placeholder="请选择" style="width: 100%">
                <el-option v-for="item in dictStore.cities" :key="item.cityNo" :label="item.cityName" :value="item.cityNo" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出发日期" prop="startDate">
              <el-date-picker v-model="itineraryForm.startDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到达日期" prop="endDate">
              <el-date-picker v-model="itineraryForm.endDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="行程说明" prop="reason">
              <el-input type="textarea" v-model="itineraryForm.reason" maxlength="500" show-word-limit placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="itineraryVisible = false">取消</el-button>
          <el-button type="primary" @click="saveItinerary">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 补助信息弹窗 -->
    <el-dialog v-model="subsidyVisible" title="补助日历" width="1000px" destroy-on-close fullscreen>
      <div class="subsidy-calendar-container">
        <div class="calendar-left">
          <div class="info-item">出差类型：<span style="color: #e6a23c">{{ getBusinessTypeDisplay(formData.businessTypeId) }}</span></div>
          <div class="timeline">
            <div>开始日期：{{ currentSubsidy.startDate }}</div>
            <div class="days-bar">行程天数 {{ getCityName(currentSubsidy.startCity) }} - {{ getCityName(currentSubsidy.endCity) }} : {{ currentSubsidy.days }}天</div>
            <div>结束日期：{{ currentSubsidy.endDate }}</div>
          </div>
          <div class="amount-summary">
            <div>申请金额: CNY {{ currentSubsidyApplyAmount.toFixed(2) }}</div>
            <div>标准总额: CNY {{ currentSubsidyStandardAmount.toFixed(2) }}</div>
            <div>补助金额: CNY {{ currentSubsidyAmount.toFixed(2) }}</div>
          </div>
        </div>
        <div class="calendar-right">
          <el-table :data="currentSubsidy.calendar" style="width: 100%" border>
            <el-table-column width="150">
              <template #header>
                <el-checkbox v-model="calendarSelectAll" @change="handleCalendarSelectAll" /> 全选
              </template>
              <template #default="{ row }">
                <el-checkbox v-model="row.selected" @change="handleRowSelectChange(row)" /> {{ row.date }}<br/>{{ row.weekday }}
              </template>
            </el-table-column>
            <el-table-column label="补助城市" width="100">
              <template #default="{ row }">{{ getCityName(row.city) }}</template>
            </el-table-column>
            <el-table-column>
              <template #header>
                餐费补助 <el-checkbox v-model="colSelect.meal" @change="handleColSelect('meal')" />
              </template>
              <template #default="{ row }">
                <div class="cell-content">
                  <div>CNY {{ row.mealStandard.toFixed(2) }}/天</div>
                  <div style="display:flex; align-items:center;">
                    <el-checkbox v-model="row.mealSelected" @change="handleCellSelect(row)" />
                    <el-input-number v-model="row.mealAmount" :min="0" :max="row.mealStandard" :precision="2" :controls="false" :disabled="!row.mealSelected" style="width: 80px; margin-left: 5px;" />
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column>
              <template #header>
                交通补助 <el-checkbox v-model="colSelect.traffic" @change="handleColSelect('traffic')" />
              </template>
              <template #default="{ row }">
                <div class="cell-content">
                  <div>CNY {{ row.trafficStandard.toFixed(2) }}/天</div>
                  <div style="display:flex; align-items:center;">
                    <el-checkbox v-model="row.trafficSelected" @change="handleCellSelect(row)" />
                    <el-input-number v-model="row.trafficAmount" :min="0" :max="row.trafficStandard" :precision="2" :controls="false" :disabled="!row.trafficSelected" style="width: 80px; margin-left: 5px;" />
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column>
              <template #header>
                通讯补助 <el-checkbox v-model="colSelect.comm" @change="handleColSelect('comm')" />
              </template>
              <template #default="{ row }">
                <div class="cell-content">
                  <div>CNY {{ row.commStandard.toFixed(2) }}/天</div>
                  <div style="display:flex; align-items:center;">
                    <el-checkbox v-model="row.commSelected" @change="handleCellSelect(row)" />
                    <el-input-number v-model="row.commAmount" :min="0" :max="row.commStandard" :precision="2" :controls="false" :disabled="!row.commSelected" style="width: 80px; margin-left: 5px;" />
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="subsidyVisible = false">取消</el-button>
          <el-button type="primary" @click="saveSubsidy">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useDictStore } from '../stores/dict'
import { getReimbursementById, addReimbursement, updateReimbursement } from '../apis/reimbursement'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const dictStore = useDictStore()

const activeNames = ref(['1', '2', '3', '4', '5', '6'])
const currentDate = new Date().toISOString().split('T')[0]
import dayjs from 'dayjs'

const formData = reactive({
  id: '',
  reimbursementTitle: '',
  reimburserId: '',
  reimDepartmentId: '',
  reimCompanyId: '',
  businessTypeId: '',
  businessTripReason: '',
  status: 0,
  creationTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  itineraries: [],
  subsidies: [],
  apportionments: [
    { companyId: '', projectId: '', percent: 100, amount: 0 }
  ],
  remarks: ''
})

const rules = {
  reimbursementTitle: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  reimburserId: [{ required: true, message: '请选择报销人', trigger: 'change' }],
  reimDepartmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  reimCompanyId: [{ required: true, message: '请选择公司', trigger: 'change' }],
  businessTypeId: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  businessTripReason: [{ required: true, message: '请输入事由', trigger: 'blur' }]
}
const formRef = ref(null)

onMounted(async () => {
  const id = route.params.id
  console.log(id);
  
  if (id) {
    try {
      const res = await getReimbursementById(id)
      if (res) {
        // 由于前后端字段命名有的可能存在差异，先处理下回显映射
        res.title = res.reimbursementTitle
        res.employeeId = res.reimburserId
        res.departmentId = res.reimDepartmentId
        res.companyId = res.reimCompanyId
        res.reason = res.businessTripReason
        res.createTime = res.creationTime

        Object.assign(formData, res)
        
        // 【修复点】：由于我们表单上绑定的是 formData.reimbursementTitle， formData.reimburserId 等，
        // Object.assign(formData, res) 实际上已经把后端的 reimbursementTitle 等字段赋给 formData 了。
        // 但如果有些组件强绑定了 formData.title 等别名，我们在上面映射一下也是可以的。
        // 为了确保 Vue 模板上双向绑定的 reimbursementTitle, reimburserId 等有值，这里确保它们存在
        formData.reimbursementTitle = res.reimbursementTitle || res.title
        formData.reimburserId = res.reimburserId || res.employeeId
        formData.reimDepartmentId = res.reimDepartmentId || res.departmentId
        formData.reimCompanyId = res.reimCompanyId || res.companyId
        formData.businessTypeId = res.businessTypeId
        formData.businessTripReason = res.businessTripReason || res.reason

        // Ensure properties exists if backend only has partial fields
        if(!formData.itineraries) formData.itineraries = []
        if(!formData.subsidies) formData.subsidies = []
        
        // 修复补助信息回显金额和天数的计算
        formData.subsidies.forEach(sub => {
          sub.days = sub.days || (sub.calendar ? sub.calendar.length : 0)
          
          let apply = 0
          let amount = 0
          if (sub.calendar && sub.calendar.length > 0) {
            sub.calendar.forEach(cal => {
              apply += (cal.mealStandard || 0) + (cal.trafficStandard || 0) + (cal.commStandard || 0)
              amount += (cal.mealSelected ? (cal.mealAmount || 0) : 0) + 
                        (cal.trafficSelected ? (cal.trafficAmount || 0) : 0) + 
                        (cal.commSelected ? (cal.commAmount || 0) : 0)
            })
          }
          sub.applyAmount = apply
          sub.subsidyAmount = amount
        })

        if(!formData.apportionments || formData.apportionments.length === 0) formData.apportionments = [{ companyId: formData.reimCompanyId, projectId: '', percent: 100, amount: Number(formData.subsidyTotal || 0) }]
      }
    } catch(err) {
      console.error(err)
    }
  }
})

// Helpers
const getEmployeeDisplay = (id) => {
  const emp = dictStore.employees.find(e => e.reimburserId === id)
  return emp ? `${emp.reimburserName}(${emp.reimburserNo})` : ''
}
const getCityName = (id) => {
  const city = dictStore.cities.find(c => c.cityNo === id)
  return city ? city.cityName : ''
}
const getBusinessTypeDisplay = (id) => {
  const type = dictStore.businessTypes.find(t => t.businessTypeId === id)
  return type ? type.businessTypeName : ''
}

// Itinerary
const itineraryVisible = ref(false)
const itineraryFormRef = ref(null)
const editItineraryIndex = ref(-1)
const itineraryForm = reactive({
  employeeId: '',
  startCity: '',
  endCity: '',
  startDate: '',
  endDate: '',
  reason: ''
})
const itineraryRules = {
  employeeId: [{ required: true, message: '请选择出行人', trigger: 'change' }],
  startCity: [{ required: true, message: '请选择出发城市', trigger: 'change' }],
  endCity: [{ required: true, message: '请选择到达城市', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择出发日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择到达日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入说明', trigger: 'blur' }]
}

const openItineraryDialog = (row, index = -1) => {
  editItineraryIndex.value = index
  if (row) {
    Object.assign(itineraryForm, JSON.parse(JSON.stringify(row)))
  } else {
    Object.assign(itineraryForm, {
      employeeId: '', startCity: '', endCity: '', startDate: '', endDate: '', reason: ''
    })
  }
  itineraryVisible.value = true
}

const copyItinerary = (row) => {
  openItineraryDialog(row) // pass row without index to act as copy (new)
}

const deleteItinerary = (index) => {
  ElMessageBox.confirm('是否确定删除该行程信息？', '提示', { type: 'warning' }).then(() => {
    formData.itineraries.splice(index, 1)
    formData.subsidies.splice(index, 1)
    recalculateApportionment()
  })
}

const saveItinerary = () => {
  itineraryFormRef.value.validate((valid) => {
    if (valid) {
      if (itineraryForm.endDate < itineraryForm.startDate) {
        return ElMessage.error('到达日期不能早于出发日期')
      }
      if (itineraryForm.endDate > currentDate) {
        return ElMessage.error('到达日期不能晚于当前日期')
      }
      
      // Check overlap
      const start = new Date(itineraryForm.startDate).getTime()
      const end = new Date(itineraryForm.endDate).getTime()
      const overlap = formData.itineraries.some((item, idx) => {
        if (idx === editItineraryIndex.value) return false
        if (item.employeeId !== itineraryForm.employeeId) return false
        const s = new Date(item.startDate).getTime()
        const e = new Date(item.endDate).getTime()
        return Math.max(start, s) <= Math.min(end, e)
      })
      if (overlap) {
        return ElMessage.error('该人员行程日期存在重复，请重新选择')
      }

      const days = Math.floor((end - start) / (1000 * 3600 * 24)) + 1

      if (editItineraryIndex.value > -1) {
        formData.itineraries[editItineraryIndex.value] = { ...itineraryForm }
        // Regenerate subsidy if dates/city changed
        generateSubsidy(editItineraryIndex.value, { ...itineraryForm }, days)
      } else {
        formData.itineraries.push({ ...itineraryForm })
        generateSubsidy(formData.itineraries.length - 1, { ...itineraryForm }, days)
      }
      recalculateApportionment()
      itineraryVisible.value = false
    }
  })
}

const getSubsidyStandard = (cityNo) => {
  const city = dictStore.cities.find(c => c.cityNo === cityNo)
  const type = city ? city.cityType : '3'
  const meal = type === '1' ? 100 : type === '2' ? 80 : 50
  return { meal, traffic: 40, comm: 40 }
}

const generateSubsidy = (index, itinerary, days) => {
  const standard = getSubsidyStandard(itinerary.endCity)
  const calendar = []
  let current = new Date(itinerary.startDate)
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  
  for (let i = 0; i < days; i++) {
    calendar.push({
      date: current.toISOString().split('T')[0],
      weekday: weekdays[current.getDay()],
      city: itinerary.endCity,
      selected: true,
      mealSelected: true, mealStandard: standard.meal, mealAmount: standard.meal,
      trafficSelected: true, trafficStandard: standard.traffic, trafficAmount: standard.traffic,
      commSelected: true, commStandard: standard.comm, commAmount: standard.comm
    })
    current.setDate(current.getDate() + 1)
  }

  const applyAmount = days * (standard.meal + standard.traffic + standard.comm)

  formData.subsidies[index] = {
    employeeId: itinerary.employeeId,
    startDate: itinerary.startDate,
    endDate: itinerary.endDate,
    startCity: itinerary.startCity,
    endCity: itinerary.endCity,
    days,
    applyAmount,
    subsidyAmount: applyAmount,
    calendar
  }
}

// Subsidy Calendar
const subsidyVisible = ref(false)
const currentSubsidy = ref(null)
const currentSubsidyIndex = ref(-1)

const openSubsidyDialog = (row, index) => {
  currentSubsidyIndex.value = index
  currentSubsidy.value = JSON.parse(JSON.stringify(row))
  updateCalendarSelectionState()
  subsidyVisible.value = true
}

const currentSubsidyStandardAmount = computed(() => {
  if (!currentSubsidy.value) return 0
  return currentSubsidy.value.calendar.reduce((sum, row) => {
    return sum + (row.mealSelected ? row.mealStandard : 0) + (row.trafficSelected ? row.trafficStandard : 0) + (row.commSelected ? row.commStandard : 0)
  }, 0)
})

const currentSubsidyApplyAmount = computed(() => {
  if (!currentSubsidy.value) return 0
  return currentSubsidy.value.calendar.reduce((sum, row) => {
    return sum + row.mealStandard + row.trafficStandard + row.commStandard
  }, 0)
})

const currentSubsidyAmount = computed(() => {
  if (!currentSubsidy.value) return 0
  return currentSubsidy.value.calendar.reduce((sum, row) => {
    return sum + (row.mealSelected ? row.mealAmount : 0) + (row.trafficSelected ? row.trafficAmount : 0) + (row.commSelected ? row.commAmount : 0)
  }, 0)
})

const calendarSelectAll = ref(true)
const colSelect = reactive({ meal: true, traffic: true, comm: true })

const handleCalendarSelectAll = (val) => {
  colSelect.meal = val
  colSelect.traffic = val
  colSelect.comm = val
  currentSubsidy.value.calendar.forEach(row => {
    row.selected = val
    row.mealSelected = val
    row.trafficSelected = val
    row.commSelected = val
    if (!val) {
      row.mealAmount = 0
      row.trafficAmount = 0
      row.commAmount = 0
    } else {
      row.mealAmount = row.mealStandard
      row.trafficAmount = row.trafficStandard
      row.commAmount = row.commStandard
    }
  })
}

const handleRowSelectChange = (row) => {
  row.mealSelected = row.selected
  row.trafficSelected = row.selected
  row.commSelected = row.selected
  if (!row.selected) {
    row.mealAmount = 0
    row.trafficAmount = 0
    row.commAmount = 0
  } else {
    row.mealAmount = row.mealStandard
    row.trafficAmount = row.trafficStandard
    row.commAmount = row.commStandard
  }
  updateCalendarSelectionState()
}

const handleColSelect = (type) => {
  const val = colSelect[type]
  currentSubsidy.value.calendar.forEach(row => {
    row[`${type}Selected`] = val
    if (!val) {
      row[`${type}Amount`] = 0
    } else {
      row[`${type}Amount`] = row[`${type}Standard`]
    }
    row.selected = row.mealSelected && row.trafficSelected && row.commSelected
  })
  updateCalendarSelectionState()
}

const handleCellSelect = (row) => {
  if (!row.mealSelected) row.mealAmount = 0
  else if(row.mealAmount === 0) row.mealAmount = row.mealStandard
  
  if (!row.trafficSelected) row.trafficAmount = 0
  else if(row.trafficAmount === 0) row.trafficAmount = row.trafficStandard
  
  if (!row.commSelected) row.commAmount = 0
  else if(row.commAmount === 0) row.commAmount = row.commStandard

  row.selected = row.mealSelected && row.trafficSelected && row.commSelected
  updateCalendarSelectionState()
}

const updateCalendarSelectionState = () => {
  if (!currentSubsidy.value) return
  calendarSelectAll.value = currentSubsidy.value.calendar.every(r => r.mealSelected && r.trafficSelected && r.commSelected)
  colSelect.meal = currentSubsidy.value.calendar.every(r => r.mealSelected)
  colSelect.traffic = currentSubsidy.value.calendar.every(r => r.trafficSelected)
  colSelect.comm = currentSubsidy.value.calendar.every(r => r.commSelected)
}

const saveSubsidy = () => {
  currentSubsidy.value.subsidyAmount = currentSubsidyAmount.value
  currentSubsidy.value.applyAmount = currentSubsidyStandardAmount.value
  formData.subsidies[currentSubsidyIndex.value] = JSON.parse(JSON.stringify(currentSubsidy.value))
  recalculateApportionment()
  subsidyVisible.value = false
}

// Totals
const totalSubsidy = computed(() => formData.subsidies.reduce((sum, item) => sum + item.subsidyAmount, 0))
const totalDays = computed(() => formData.subsidies.reduce((sum, item) => sum + item.days, 0))
const totalMeal = computed(() => formData.subsidies.reduce((sum, item) => sum + item.calendar.reduce((s, r) => s + (r.mealSelected ? r.mealAmount : 0), 0), 0))
const totalTraffic = computed(() => formData.subsidies.reduce((sum, item) => sum + item.calendar.reduce((s, r) => s + (r.trafficSelected ? r.trafficAmount : 0), 0), 0))
const totalComm = computed(() => formData.subsidies.reduce((sum, item) => sum + item.calendar.reduce((s, r) => s + (r.commSelected ? r.commAmount : 0), 0), 0))

// Apportionment
watch(totalSubsidy, (newVal) => {
  recalculateApportionment()
})

const addApportionment = () => {
  formData.apportionments.push({ companyId: '', projectId: '', percent: 0, amount: 0 })
  recalculateApportionmentPercent()
}

const deleteApportionment = (index) => {
  if (formData.apportionments.length <= 1) {
    return ElMessage.warning('至少保留一条分摊信息')
  }
  ElMessageBox.confirm('是否确定删除该行数据？', '提示', { type: 'warning' }).then(() => {
    formData.apportionments.splice(index, 1)
    recalculateApportionmentPercent()
  })
}

const evenApportion = () => {
  const count = formData.apportionments.length
  if (count === 0) return
  const avgPercent = Math.floor(10000 / count) / 100
  const avgAmount = Math.floor(totalSubsidy.value * 100 / count) / 100
  
  let pSum = 0
  let aSum = 0
  for (let i = 1; i < count; i++) {
    formData.apportionments[i].percent = avgPercent
    formData.apportionments[i].amount = avgAmount
    pSum += avgPercent
    aSum += avgAmount
  }
  formData.apportionments[0].percent = Number((100 - pSum).toFixed(2))
  formData.apportionments[0].amount = Number((totalSubsidy.value - aSum).toFixed(2))
}

const handleApportionPercentChange = (index) => {
  recalculateApportionmentPercent()
}

const recalculateApportionmentPercent = () => {
  if (formData.apportionments.length === 0) return
  if (formData.apportionments.length === 1) {
    formData.apportionments[0].percent = 100
    formData.apportionments[0].amount = totalSubsidy.value
    return
  }

  let pSum = 0
  for (let i = 1; i < formData.apportionments.length; i++) {
    pSum += formData.apportionments[i].percent
  }

  if (pSum > 100) {
    // reset current modified or just clear all >0
    formData.apportionments.forEach((app, idx) => {
      if (idx > 0) app.percent = 0
    })
    pSum = 0
  }

  formData.apportionments[0].percent = Number((100 - pSum).toFixed(2))
  
  // Recalculate amount
  let aSum = 0
  for (let i = 1; i < formData.apportionments.length; i++) {
    const amount = Number((totalSubsidy.value * formData.apportionments[i].percent / 100).toFixed(2))
    formData.apportionments[i].amount = amount
    aSum += amount
  }
  formData.apportionments[0].amount = Number((totalSubsidy.value - aSum).toFixed(2))
}

const recalculateApportionment = () => {
  recalculateApportionmentPercent()
}

const clearRemarks = () => {
  ElMessageBox.confirm('是否确定删除备注？', '提示', { type: 'warning' }).then(() => {
    formData.remarks = ''
  })
}

const handleClose = () => {
  ElMessageBox.confirm('是否确定关闭当前页面？未保存的数据将丢失', '提示', { type: 'warning' }).then(() => {
    router.back()
  })
}

const handleSaveDraft = () => {
  formData.status = 0
  formData.subsidyTotal = String(totalSubsidy.value.toFixed(2))
  formData.mealAllowance = String(totalMeal.value.toFixed(2))
  formData.transportationAllowance = String(totalTraffic.value.toFixed(2))
  formData.phoneAllowance = String(totalComm.value.toFixed(2))
  
  const emp = dictStore.employees.find(e => e.reimburserId === formData.reimburserId)
  if(emp) { formData.reimburserName = emp.reimburserName; formData.reimburserNo = emp.reimburserNo; }
  
  const dept = dictStore.departments.find(d => d.reimDepartmentId === formData.reimDepartmentId)
  if(dept) { formData.reimDepartmentName = dept.reimDepartmentName; formData.reimDepartmentNo = dept.reimDepartmentNo; }
  
  const comp = dictStore.companies.find(c => c.reimCompanyId === formData.reimCompanyId)
  if(comp) { formData.reimCompanyName = comp.reimCompanyName; formData.reimCompanyNo = comp.reimCompanyNo; }
  
  const type = dictStore.businessTypes.find(t => t.businessTypeId === formData.businessTypeId)
  if(type) { formData.businessTypeName = type.businessTypeName; formData.businessTypeNo = type.businessTypeNo; }

  const payload = JSON.parse(JSON.stringify(formData))
  // 【移除】因为前端模板 v-model 绑定的已经是 formData.reimbursementTitle 等正确的数据库字段了
  // 所以不需要再做别名映射赋值，避免覆盖掉真正的值
  
  const request = formData.id ? updateReimbursement(payload) : addReimbursement(payload)
  
  request.then(() => {
    ElMessage.success('保存草稿成功')
    router.push('/')
  }).catch(err => console.error(err))
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      if (formData.itineraries.length === 0) {
        return ElMessage.warning('请至少添加一条补录行程')
      }
      
      const appValid = formData.apportionments.every(a => a.companyId)
      if (!appValid) {
        return ElMessage.warning('费用归属公司为必填项')
      }
      
      let pSum = formData.apportionments.reduce((sum, item) => sum + item.percent, 0)
      if (Math.abs(pSum - 100) > 0.01) {
        return ElMessage.warning('分摊比例合计必须为100%')
      }

      let aSum = formData.apportionments.reduce((sum, item) => sum + item.amount, 0)
      if (Math.abs(aSum - totalSubsidy.value) > 0.01) {
        return ElMessage.warning('分摊金额合计必须等于补助总金额')
      }

      formData.status = 1
      formData.subsidyTotal = String(totalSubsidy.value.toFixed(2))
      formData.mealAllowance = String(totalMeal.value.toFixed(2))
      formData.transportationAllowance = String(totalTraffic.value.toFixed(2))
      formData.phoneAllowance = String(totalComm.value.toFixed(2))
      
      const emp = dictStore.employees.find(e => e.reimburserId === formData.reimburserId)
      if(emp) { formData.reimburserName = emp.reimburserName; formData.reimburserNo = emp.reimburserNo; }
      
      const dept = dictStore.departments.find(d => d.reimDepartmentId === formData.reimDepartmentId)
      if(dept) { formData.reimDepartmentName = dept.reimDepartmentName; formData.reimDepartmentNo = dept.reimDepartmentNo; }
      
      const comp = dictStore.companies.find(c => c.reimCompanyId === formData.reimCompanyId)
      if(comp) { formData.reimCompanyName = comp.reimCompanyName; formData.reimCompanyNo = comp.reimCompanyNo; }
      
      const type = dictStore.businessTypes.find(t => t.businessTypeId === formData.businessTypeId)
      if(type) { formData.businessTypeName = type.businessTypeName; formData.businessTypeNo = type.businessTypeNo; }

      const payload = JSON.parse(JSON.stringify(formData))
      
      const request = formData.id ? updateReimbursement(payload) : addReimbursement(payload)
      
      request.then(() => {
        ElMessageBox.alert('提交成功', '提示', {
          confirmButtonText: '确定',
          callback: () => {
            router.push('/')
          }
        })
      }).catch(err => console.error(err))
    }
  })
}

</script>

<style scoped>
.detail-container {
  padding-bottom: 80px;
  background-color: #f0f2f5;
  min-height: 100vh;
}
.header-fixed {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}
.header-title {
  font-size: 20px;
  font-weight: bold;
  flex: 1;
  text-align: center;
}
.header-date {
  font-size: 14px;
  color: #666;
}
.form-content {
  width: 1200px;
  margin: 20px auto;
  background: #fff;
  padding: 20px;
}
.collapse-title-custom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 20px;
  font-size: 16px;
  font-weight: bold;
}
.subsidy-total-hint {
  font-size: 14px;
  color: #666;
  font-weight: normal;
  margin-left: 10px;
}
.fee-total-row {
  font-size: 14px;
  padding: 10px 0;
}
.apportion-footer {
  display: flex;
  justify-content: flex-end;
  gap: 100px;
  padding: 15px 50px;
  background: #fff8e6;
  margin-top: 10px;
  color: #e6a23c;
  font-weight: bold;
}
.footer-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 15px 0;
  text-align: center;
  box-shadow: 0 -2px 12px 0 rgba(0,0,0,.1);
  z-index: 100;
}
.subsidy-calendar-container {
  display: flex;
  height: 60vh;
}
.calendar-left {
  width: 200px;
  border-right: 1px solid #ebeef5;
  padding-right: 20px;
  margin-right: 20px;
}
.calendar-right {
  flex: 1;
  overflow-y: auto;
}
.timeline {
  margin: 20px 0;
  border-left: 2px solid #409EFF;
  padding-left: 10px;
}
.days-bar {
  background: #409EFF;
  color: #fff;
  padding: 5px;
  margin: 10px 0;
  font-size: 12px;
}
.amount-summary {
  margin-top: 50px;
  line-height: 2;
}
.cell-content {
  text-align: center;
}
:deep(.el-collapse-item__header) {
  font-size: 16px;
  font-weight: bold;
}
:deep(.el-form-item__label) {
  font-size: 14px;
}
:deep(.el-table) {
  font-size: 14px;
}
</style>
