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
            <div class="collapse-title-custom title-base">基础信息</div>
          </template>
          <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px" class="base-form" :disabled="isReadOnly">
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
            <div class="collapse-title-custom title-base">
              <span>补录行程</span>
              <el-button type="primary" link @click.stop="openItineraryDialog()" :disabled="isReadOnly">⊕ 补录行程</el-button>
            </div>
          </template>
          <el-table :data="formData.itineraries" style="width: 100%" :header-cell-style="{ backgroundColor: '#f5f7fa', color: '#606266', fontWeight: 'normal' }">
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
                <el-button type="primary" link @click="deleteItinerary($index)" :disabled="isReadOnly"><el-icon><Delete /></el-icon></el-button>
                <el-button type="primary" link @click="openItineraryDialog(row, $index)" :disabled="isReadOnly"><el-icon><EditPen /></el-icon></el-button>
                <el-button type="primary" link @click="copyItinerary(row)" :disabled="isReadOnly"><el-icon><CopyDocument /></el-icon></el-button>
                
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>

        <!-- 补助信息 -->
        <el-collapse-item name="3">
          <template #title>
            <div class="">
              <span>补助信息</span>
              <span class="subsidy-total-hint">{{ totalSubsidy.toFixed(2) }} (补助天数: {{ totalDays }}天)</span>
            </div>
          </template>
          <el-alert type="warning" show-icon :closable="false" style="margin-bottom: 10px;" class="black-text-alert">
            <template #title>
              <span>1、请根据实际出差日期选择补助 2、出差期间当日有用餐安排的请自行核减当日餐补 3、出差期间当日有用车的，请自行核减当日交补</span>
            </template>
          </el-alert>
          <el-table :data="formData.subsidies" style="width: 100%" :header-cell-style="{ backgroundColor: '#f5f7fa', color: '#606266', fontWeight: 'normal' }">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column label="出行人">
              <template #default="{ row }">{{ getEmployeeDisplay(row.employeeId) }}</template>
            </el-table-column>
            <el-table-column label="出差日期" min-width="130">
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
                <el-button type="primary" link @click="openSubsidyDialog(row, $index)" :disabled="isReadOnly"><el-icon><EditPen /></el-icon></el-button>
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
            <div class="">
              <span>费用归属及分摊</span>
              <span class="subsidy-total-hint">(分摊金额: {{ totalSubsidy.toFixed(2) }})</span>
            </div>
          </template>
          <el-table :data="formData.apportionments" style="width: 100%" :header-cell-style="{ backgroundColor: '#f5f7fa', color: '#606266', fontWeight: 'normal' }">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column>
              <template #header>
                费用归属<span style="color: #f56c6c">*</span>
              </template>
              <template #default="{ row, $index }">
                <el-select v-model="row.companyId" placeholder="请选择" style="width: 100%" :disabled="isReadOnly || ($index === 0 && false)">
                  <el-option v-for="item in dictStore.companies" :key="item.reimCompanyId" :label="item.reimCompanyName" :value="item.reimCompanyId" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="项目">
              <template #default="{ row }">
                <el-select v-model="row.projectId" placeholder="请选择" style="width: 100%" clearable :disabled="isReadOnly">
                  <el-option v-for="item in dictStore.projects" :key="item.projectId" :label="item.projectName" :value="item.projectId" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column align="right">
              <template #header>
                <div style="display: flex; align-items: center; justify-content: flex-end;">
                  <span>分摊比例(%)</span>
                  <el-icon @click="evenApportion" :class="['refresh-icon', { 'refresh-icon-disabled': isReadOnly }]"><Refresh /></el-icon>
                  <span style="color: #f56c6c">*</span>
                </div>
              </template>
              <template #default="{ row, $index }">
                <el-input-number v-model="row.percent" :min="0" :max="100" :precision="2" :controls="false" :disabled="isReadOnly || $index === 0" @change="handleApportionPercentChange($index)" style="width: 100%" class="percent-input right-align-input" />
              </template>
            </el-table-column>
            <el-table-column align="right">
              <template #header>
                <div style="text-align: right;">
                  分摊金额<span style="color: #f56c6c">*</span>
                </div>
              </template>
              <template #default="{ row }">
                <el-input-number v-model="row.amount" :disabled="true" :precision="2" :controls="false" style="width: 100%" class="right-align-input" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button type="primary" link @click="deleteApportionment($index)" :disabled="isReadOnly"><el-icon><Delete /></el-icon></el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="text-align: center; margin-top: 10px;">
            <el-button type="primary" link @click="addApportionment" :disabled="isReadOnly">⊕ 添加一行</el-button>
          </div>
          <div class="apportion-footer">
            <span style="color: #666;">合计</span>
            <span>100.00%</span>
            <span>CNY {{ totalSubsidy.toFixed(2) }}</span>
          </div>
        </el-collapse-item>

        <!-- 备注信息 -->
        <el-collapse-item name="6">
          <template #title>
            <div class="collapse-title-custom">
              <span>备注信息</span>
              <el-button type="primary" link @click.stop="clearRemarks" :disabled="isReadOnly"><el-icon><Delete /></el-icon>删除备注</el-button>
            </div>
          </template>
          <el-input type="textarea" v-model="formData.remarks" maxlength="1000" show-word-limit placeholder="请输入备注信息" :rows="4" :disabled="isReadOnly" />
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- Footer Buttons -->
    <div class="footer-fixed">
      <el-button @click="handleClose" class="persistent-blue-btn">关闭</el-button>
      <el-button type="primary" @click="handleSubmit" :disabled="isReadOnly">提交</el-button>
    </div>

    <!-- 补录行程弹窗 -->
    <el-dialog v-model="itineraryVisible" title="补录行程" width="800px" destroy-on-close>
      <el-alert type="warning" show-icon :closable="false" class="custom-alert" style="margin-bottom: 15px; align-items: flex-start;">
        <template #title>
          <div style="font-size: 14px; color: #606266; line-height: 1.5; margin-top: -2px;">仅可补录未从申请单带入或未产生费用的行程信息</div>
        </template>
        <div style="font-size: 14px; color: #606266; line-height: 1.5;">跨天跨城行程填写说明： 出发城市-到达城市：武汉-北京; 出发日期-到达日期：1号-5号; 1号~5号补助按北京匹配;</div>
      </el-alert>
      <el-form :model="itineraryForm" :rules="itineraryRules" ref="itineraryFormRef" label-width="120px" class="itinerary-form-custom" :disabled="isReadOnly">
        <el-row>
          <el-col :span="16">
            <el-form-item label="出行人" prop="employeeId">
              <el-select v-model="itineraryForm.employeeId" placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="item in dictStore.employees" :key="item.reimburserId" :label="item.reimburserName" :value="item.reimburserId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="出发城市" prop="startCity">
              <el-select v-model="itineraryForm.startCity" placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="item in dictStore.cities" :key="item.cityNo" :label="item.cityName" :value="item.cityNo" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="到达城市" prop="endCity">
              <el-select v-model="itineraryForm.endCity" placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="item in dictStore.cities" :key="item.cityNo" :label="item.cityName" :value="item.cityNo" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="14">
            <el-form-item label="出发到达日期" prop="dateRange">
              <el-date-picker v-model="itineraryForm.dateRange" type="datetimerange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD HH:mm:ss" :disabled-date="disableFutureDate" style="width: 100%" />
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
          <el-button @click="itineraryVisible = false" class="persistent-blue-btn">取消</el-button>
          <el-button type="primary" @click="saveItinerary" :disabled="isReadOnly">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 补助信息弹窗 -->
    <el-dialog v-model="subsidyVisible" title="补助日历" width="1000px" destroy-on-close fullscreen>
      <div class="subsidy-calendar-container">
        <div class="calendar-left">
          <div class="type-header">
            <span class="type-label">出差类型</span>
            <span class="type-value">{{ getBusinessTypeDisplay(formData.businessTypeId) }}</span>
          </div>
          
          <div class="itinerary-card">
            <div class="itinerary-row">
              <div class="row-label">开始日期</div>
              <div class="timeline-node node-top">
                <div class="donut-dot"></div>
                <div class="line line-down"></div>
              </div>
              <div class="row-value">{{ currentSubsidy.startDate }}</div>
            </div>
            
            <div class="blue-bar">
              <div class="row-label" style="color: #fff;">行程天数</div>
              <div class="timeline-node"></div>
              <div class="row-value" style="color: #fff;">
                <div style="display: flex; justify-content: space-between;">
                  <span>{{ getCityName(currentSubsidy.startCity) }} - {{ getCityName(currentSubsidy.endCity) }}</span>
                  <span>{{ currentSubsidy.days }}天</span>
                </div>
              </div>
            </div>
            
            <div class="itinerary-row">
              <div class="row-label">结束日期</div>
              <div class="timeline-node node-bottom">
                <div class="line line-up"></div>
                <div class="donut-dot"></div>
              </div>
              <div class="row-value">{{ currentSubsidy.endDate }}</div>
            </div>
          </div>

          <div class="amount-card">
            <div class="amount-row">
              <span class="amount-label">申请金额</span>
              <span class="amount-value">
                <span class="currency">CNY</span>
                <!-- <span class="number">{{ currentSubsidyApplyAmount.toFixed(2) }}</span> -->
                <span class="number">{{ currentSubsidyAmount.toFixed(2) }}</span>
              </span>
            </div>
            <div class="amount-row">
              <span class="amount-label">标准总额</span>
              <span class="amount-value">
                <span class="currency">CNY</span>
                <span class="number">{{ currentSubsidyStandardAmount.toFixed(2) }}</span>
              </span>
            </div>
            <div class="amount-row">
              <span class="amount-label">补助金额</span>
              <span class="amount-value">
                <span class="currency">CNY</span>
                <span class="number">{{ currentSubsidyAmount.toFixed(2) }}</span>
              </span>
            </div>
          </div>
        </div>
        <div class="calendar-right">
          <div class="right-header">
            <span class="right-title">出差补助</span>
            <el-checkbox v-model="calendarSelectAll" @change="handleCalendarSelectAll" class="right-checkbox" :disabled="isReadOnly">全选</el-checkbox>
          </div>
          <el-table :data="currentSubsidy.calendar" style="width: 100%" border class="subsidy-table" :header-cell-style="{ backgroundColor: '#f5f7fa', color: '#606266', fontWeight: 'normal' }">
            <el-table-column label="出差日期" width="160" align="center">
              <template #default="{ row }">
                <div class="date-cell">
                  <div class="date-text">
                    <div style="color: #606266; font-size: 13px; margin-bottom: 2px;">{{ row.date }}</div>
                    <div style="color: #909399; font-size: 13px;">{{ row.weekday }} <el-checkbox v-model="row.selected" @change="handleRowSelectChange(row)" style="margin-left: 5px; height: 14px;" :disabled="isReadOnly" /></div>
                  </div>
                  <el-icon class="location-icon"><Location /></el-icon>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="补助城市" align="center">
              <template #default="{ row }">{{ getCityName(row.city) }}</template>
            </el-table-column>
            <el-table-column align="center">
              <template #header>
                <div class="custom-header">餐费补助 <el-checkbox v-model="colSelect.meal" @change="handleColSelect('meal')" :disabled="isReadOnly" /></div>
              </template>
              <template #default="{ row }">
                <div class="cell-content">
                  <div class="standard-text">CNY {{ row.mealStandard.toFixed(2) }} / 天</div>
                  <div class="input-row">
                    <el-checkbox v-model="row.mealSelected" @change="handleCellSelect(row)" :disabled="isReadOnly" />
                    <el-input-number v-model="row.mealAmount" :min="0" :max="row.mealStandard" :precision="2" :controls="false" :disabled="isReadOnly || !row.mealSelected" class="amount-input" />
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column align="center">
              <template #header>
                <div class="custom-header">交通补助 <el-checkbox v-model="colSelect.traffic" @change="handleColSelect('traffic')" :disabled="isReadOnly" /></div>
              </template>
              <template #default="{ row }">
                <div class="cell-content">
                  <div class="standard-text">CNY {{ row.trafficStandard.toFixed(2) }} / 天</div>
                  <div class="input-row">
                    <el-checkbox v-model="row.trafficSelected" @change="handleCellSelect(row)" :disabled="isReadOnly" />
                    <el-input-number v-model="row.trafficAmount" :min="0" :max="row.trafficStandard" :precision="2" :controls="false" :disabled="isReadOnly || !row.trafficSelected" class="amount-input" />
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column align="center">
              <template #header>
                <div class="custom-header">通讯补助 <el-checkbox v-model="colSelect.comm" @change="handleColSelect('comm')" :disabled="isReadOnly" /></div>
              </template>
              <template #default="{ row }">
                <div class="cell-content">
                  <div class="standard-text">CNY {{ row.commStandard.toFixed(2) }} / 天</div>
                  <div class="input-row">
                    <el-checkbox v-model="row.commSelected" @change="handleCellSelect(row)" :disabled="isReadOnly" />
                    <el-input-number v-model="row.commAmount" :min="0" :max="row.commStandard" :precision="2" :controls="false" :disabled="isReadOnly || !row.commSelected" class="amount-input" />
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer sub-footer">
          <el-button @click="subsidyVisible = false" class="persistent-blue-btn">取消</el-button>
          <el-button type="primary" @click="saveSubsidy" :disabled="isReadOnly">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * @cn-file
 * @file src/views/ReimbursementDetail.vue
 * @desc 页面：业务页面逻辑与交互
 */

import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useDictStore } from '../stores/dict'
import { getReimbursementById, addReimbursement, updateReimbursement } from '../apis/reimbursement'
import { ElMessage, ElMessageBox } from 'element-plus'
import { WarningFilled, Location, Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const dictStore = useDictStore()

const activeNames = ref(['1', '2', '3', '4', '5', '6'])
const currentDate = new Date().toISOString().split('T')[0]
import dayjs from 'dayjs'
/**
 * 禁用未来日期，避免选择晚于当前时间的行程日期。
 */
const disableFutureDate = (date) => date.getTime() > Date.now()

const formData = reactive({
  id: '',
  title: '',
  reimbursementTitle: '',
  employeeId: '',
  reimburserId: '',
  departmentId: '',
  reimDepartmentId: '',
  companyId: '',
  reimCompanyId: '',
  businessTypeId: '',
  reason: '',
  businessTripReason: '',
  status: 0,
  createTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  creationTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  itineraries: [],
  subsidies: [],
  apportionments: [
    { companyId: '', projectId: '', percent: 100, amount: 0 }
  ],
  remarks: ''
})

// const rules = {
//   title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
//   employeeId: [{ required: true, message: '请选择报销人', trigger: 'change' }],
//   departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
//   companyId: [{ required: true, message: '请选择公司', trigger: 'change' }],
//   businessTypeId: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
//   reason: [{ required: true, message: '请输入事由', trigger: 'blur' }]
// }
const rules = {
  companyId: [{ required: true, message: '请选择公司', trigger: 'change' }],
  businessTypeId: [{ required: true, message: '请选择业务类型', trigger: 'change' }]
}
const formRef = ref(null)
const isReadOnly = computed(() => formData.status === 1 || formData.status === 2)
let submitDebounceTimer = null

/**
 * 页面初始化时根据路由参数加载报销详情，并处理表单回显映射。
 */
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
/**
 * 根据报销人 ID 生成“姓名(工号)”格式的展示文本。
 */
const getEmployeeDisplay = (id) => {
  const emp = dictStore.employees.find(e => e.reimburserId === id)
  return emp ? `${emp.reimburserName}(${emp.reimburserNo})` : ''
}
/**
 * 根据城市编码获取城市名称。
 */
const getCityName = (id) => {
  const city = dictStore.cities.find(c => c.cityNo === id)
  return city ? city.cityName : ''
}
/**
 * 根据业务类型 ID 获取业务类型名称。
 */
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
  dateRange: [],
  startDate: '',
  endDate: '',
  reason: ''
})
const itineraryRules = {
  employeeId: [{ required: true, message: '请选择出行人', trigger: 'change' }],
  startCity: [{ required: true, message: '请选择出发城市', trigger: 'change' }],
  endCity: [{ required: true, message: '请选择到达城市', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择出发到达日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入行程说明', trigger: 'blur' }]
}

/**
 * 打开补录行程弹窗，支持新增和编辑两种模式。
 */
const openItineraryDialog = (row, index = -1) => {
  if (isReadOnly.value) return
  editItineraryIndex.value = index
  if (row) {
    Object.assign(itineraryForm, JSON.parse(JSON.stringify(row)))
    itineraryForm.dateRange = [
      row.startDate.includes(':') ? row.startDate : row.startDate + ' 00:00:00',
      row.endDate.includes(':') ? row.endDate : row.endDate + ' 00:00:00'
    ]
  } else {
    Object.assign(itineraryForm, {
      employeeId: '', startCity: '', endCity: '', startDate: '', endDate: '', reason: '', dateRange: []
    })
  }
  itineraryVisible.value = true
}

/**
 * 将指定文本复制到剪贴板，兼容现代接口与旧版降级方案。
 */
const copyTextToClipboard = async (text) => {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text)
    return
  }

  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', 'readonly')
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  document.execCommand('copy')
  document.body.removeChild(textarea)
}

/**
 * 复制单条行程信息，便于用户快速复用或外部粘贴。
 */
const copyItinerary = async (row) => {
  if (isReadOnly.value) return

  const content = [
    `出行人员：${getEmployeeDisplay(row.employeeId)}`,
    `出差日期：${row.startDate} 至 ${row.endDate}`,
    `行程：${getCityName(row.startCity)} - ${getCityName(row.endCity)}`,
    `行程说明：${row.reason || ''}`
  ].join('\n')

  try {
    await copyTextToClipboard(content)
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    console.error(err)
    ElMessage.error('复制失败')
  }
}

/**
 * 删除指定行程，并同步移除对应的补助信息后重算分摊。
 */
const deleteItinerary = (index) => {
  if (isReadOnly.value) return
  ElMessageBox.confirm('是否确定删除该行程信息？', '提示', { type: 'warning' }).then(() => {
    formData.itineraries.splice(index, 1)
    formData.subsidies.splice(index, 1)
    recalculateApportionment()
  })
}

/**
 * 校验并保存补录行程，保存后自动生成对应补助数据。
 */
const saveItinerary = () => {
  if (isReadOnly.value) return
  itineraryFormRef.value.validate((valid) => {
    if (valid) {
      itineraryForm.startDate = itineraryForm.dateRange[0].split(' ')[0]
      itineraryForm.endDate = itineraryForm.dateRange[1].split(' ')[0]

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

/**
 * 根据补助城市获取餐补、交补和通讯补助标准。
 */
const getSubsidyStandard = (cityNo) => {
  const city = dictStore.cities.find(c => c.cityNo === cityNo)
  const type = city ? city.cityType : '3'
  const meal = type === '1' ? 100 : type === '2' ? 80 : 50
  return { meal, traffic: 40, comm: 40 }
}

/**
 * 根据行程信息生成逐日补助日历及补助汇总数据。
 */
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

/**
 * 打开补助编辑弹窗，并复制当前行的补助数据用于临时编辑。
 */
const openSubsidyDialog = (row, index) => {
  if (isReadOnly.value) return
  currentSubsidyIndex.value = index
  currentSubsidy.value = JSON.parse(JSON.stringify(row))
  updateCalendarSelectionState()
  subsidyVisible.value = true
}

const currentSubsidyStandardAmount = computed(() => {
  if (!currentSubsidy.value) return 0
  return currentSubsidy.value.calendar.reduce((sum, row) => {
    return sum + (row.mealStandard || 0) + (row.trafficStandard || 0) + (row.commStandard || 0)
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

/**
 * 处理补助日历全选状态，同步整行及各补助项的勾选结果。
 */
const handleCalendarSelectAll = (val) => {
  if (isReadOnly.value) return
  colSelect.meal = val
  colSelect.traffic = val
  colSelect.comm = val
  currentSubsidy.value.calendar.forEach(row => {
    row.selected = val
    row.mealSelected = val
    row.trafficSelected = val
    row.commSelected = val
    if (val) {
      row.mealAmount = row.mealStandard
      row.trafficAmount = row.trafficStandard
      row.commAmount = row.commStandard
    }
  })
}

/**
 * 处理单日行选择状态，并同步当天各类补助的勾选与金额。
 */
const handleRowSelectChange = (row) => {
  if (isReadOnly.value) return
  row.mealSelected = row.selected
  row.trafficSelected = row.selected
  row.commSelected = row.selected
  if (row.selected) {
    row.mealAmount = row.mealStandard
    row.trafficAmount = row.trafficStandard
    row.commAmount = row.commStandard
  }
  updateCalendarSelectionState()
}

/**
 * 按补助类型批量勾选或取消某一列的补助项。
 */
const handleColSelect = (type) => {
  if (isReadOnly.value) return
  const val = colSelect[type]
  currentSubsidy.value.calendar.forEach(row => {
    row[`${type}Selected`] = val
    if (val && row[`${type}Amount`] == null) {
      row[`${type}Amount`] = row[`${type}Standard`]
    }
    row.selected = row.mealSelected && row.trafficSelected && row.commSelected
  })
  updateCalendarSelectionState()
}

/**
 * 处理单元格补助勾选变化，并补齐默认金额。
 */
const handleCellSelect = (row) => {
  if (isReadOnly.value) return
  if (row.mealSelected && row.mealAmount == null) row.mealAmount = row.mealStandard
  
  if (row.trafficSelected && row.trafficAmount == null) row.trafficAmount = row.trafficStandard
  
  if (row.commSelected && row.commAmount == null) row.commAmount = row.commStandard

  row.selected = row.mealSelected && row.trafficSelected && row.commSelected
  updateCalendarSelectionState()
}

/**
 * 根据当前日历勾选结果刷新全选与列选择状态。
 */
const updateCalendarSelectionState = () => {
  if (!currentSubsidy.value) return
  calendarSelectAll.value = currentSubsidy.value.calendar.every(r => r.mealSelected && r.trafficSelected && r.commSelected)
  colSelect.meal = currentSubsidy.value.calendar.every(r => r.mealSelected)
  colSelect.traffic = currentSubsidy.value.calendar.every(r => r.trafficSelected)
  colSelect.comm = currentSubsidy.value.calendar.every(r => r.commSelected)
}

/**
 * 保存补助弹窗中的编辑结果，并回写到主表单数据。
 */
const saveSubsidy = () => {
  if (isReadOnly.value) return
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
/**
 * 监听补助总额变化，自动刷新费用分摊金额。
 */
watch(totalSubsidy, (newVal) => {
  recalculateApportionment()
})

/**
 * 新增一条费用分摊记录，并重新计算默认比例。
 */
const addApportionment = () => {
  if (isReadOnly.value) return
  formData.apportionments.push({ companyId: '', projectId: '', percent: 0, amount: 0 })
  recalculateApportionmentPercent()
}

/**
 * 删除指定分摊行，并在删除后重新计算比例与金额。
 */
const deleteApportionment = (index) => {
  if (isReadOnly.value) return
  if (formData.apportionments.length <= 1) {
    return ElMessage.warning('至少保留一条分摊信息')
  }
  ElMessageBox.confirm('是否确定删除该行数据？', '提示', { type: 'warning' }).then(() => {
    formData.apportionments.splice(index, 1)
    recalculateApportionmentPercent()
  })
}

/**
 * 将当前分摊记录按条数平均分配比例和金额。
 */
const evenApportion = () => {
  if (isReadOnly.value) return
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

/**
 * 处理分摊比例变化，并触发重新计算。
 */
const handleApportionPercentChange = (index) => {
  recalculateApportionmentPercent()
}

/**
 * 按当前分摊比例重新计算首行比例与各行分摊金额。
 */
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

/**
 * 统一封装分摊重算入口，便于行程或补助变化后调用。
 */
const recalculateApportionment = () => {
  recalculateApportionmentPercent()
}

/**
 * 清空备注信息，操作前需要用户二次确认。
 */
const clearRemarks = () => {
  if (isReadOnly.value) return
  ElMessageBox.confirm('是否确定删除备注？', '提示', { type: 'warning' }).then(() => {
    formData.remarks = ''
  })
}

/**
 * 关闭当前页面并返回上一页。
 */
const closePage = () => {
  router.back()
}

/**
 * 处理关闭按钮逻辑，支持保存草稿后退出或直接离开。
 */
const handleClose = () => {
  if (isReadOnly.value) return closePage()
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
      if (action === 'cancel') closePage()
    })
}

/**
 * 按指定状态提交报销单，并在提交前补齐后端所需字段。
 */
const doSubmit = (status) => {
  if (isReadOnly.value) return
  clearTimeout(submitDebounceTimer)
  submitDebounceTimer = setTimeout(() => {
    formData.status = status
    formData.subsidyTotal = String(totalSubsidy.value.toFixed(2))
    formData.mealAllowance = String(totalMeal.value.toFixed(2))
    formData.transportationAllowance = String(totalTraffic.value.toFixed(2))
    formData.phoneAllowance = String(totalComm.value.toFixed(2))

    formData.reimbursementTitle = formData.title
    formData.reimburserId = formData.employeeId
    formData.reimDepartmentId = formData.departmentId
    formData.reimCompanyId = formData.companyId
    formData.businessTripReason = formData.reason
    formData.creationTime = formData.createTime || formData.creationTime
    
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
      ElMessage.success(status === 0 ? '保存草稿成功' : '提交成功')
      closePage()
    }).catch(err => console.error(err))
    submitDebounceTimer = null
  }, 300)
}

/**
 * 提交前执行表单、行程和分摊信息校验，通过后正式提交。
 */
const handleSubmit = () => {
  if (isReadOnly.value) return
  formRef.value.validate((valid) => {
    if (valid) {
      if (formData.itineraries.length === 0) {
        ElMessageBox.confirm('您还未添加补录行程，是否要先保存为草稿？', '提示', {
          confirmButtonText: '保存草稿',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          doSubmit(0)
        }).catch(() => {})
        return
      }
      
      const appValid = formData.apportionments.every(a => a.companyId)
      if (!appValid) {
        ElMessageBox.confirm('费用归属公司有未填项，是否要先保存为草稿？', '提示', {
          confirmButtonText: '保存草稿',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          doSubmit(0)
        }).catch(() => {})
        return
      }
      
      // Check apportionments
      let pSum = formData.apportionments.reduce((sum, item) => sum + item.percent, 0)
      if (Math.abs(pSum - 100) > 0.01) {
        ElMessageBox.confirm('分摊比例合计不为100%，是否要先保存为草稿？', '提示', {
          confirmButtonText: '保存草稿',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => doSubmit(0)).catch(() => {})
        return
      }
      
      let aSum = formData.apportionments.reduce((sum, item) => sum + item.amount, 0)
      if (Math.abs(aSum - totalSubsidy.value) > 0.01) {
        ElMessageBox.confirm('分摊金额合计不等于补助总额，是否要先保存为草稿？', '提示', {
          confirmButtonText: '保存草稿',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => doSubmit(0)).catch(() => {})
        return
      }

      // All Valid -> Status 1
      doSubmit(1)
    } else {
      ElMessageBox.confirm('表单有必填项未填完，是否要先保存为草稿？', '提示', {
        confirmButtonText: '保存草稿',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        doSubmit(0)
      }).catch(() => {})
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
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}
.header-title {
  font-size: 20px;
  font-weight: bold;
}
.header-date {
  position: absolute;
  right: 30px;
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
  padding-right: 15px;
  font-size: 16px;
  font-weight: normal;
}
.subsidy-total-hint {
  font-size: 14px;
  color: #666;
  font-weight: normal;
  margin-left: 10px;
}
.fee-total-row {
  font-size: 14px;
  padding: 10px 50px;
}
.apportion-footer {
  display: flex;
  justify-content: space-between;
  gap: 100px;
  padding: 15px 50px 15px 15px;
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
  padding: 10px 0;
  border: 1px solid #ebeef5;
}
.calendar-left {
  width: 280px;
  /* border-right: 1px solid #ebeef5; */
  padding-right: 20px;
  /* margin-right: 20px; */
}
.calendar-right {
  flex: 1;
  overflow-y: auto;
}
.right-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  /* background-color: #f5f7fa; */
  /* padding: 10px 15px; */
  /* border: 1px solid #ebeef5; */
  border-bottom: none;
}
.right-title {
  font-size: 16px;
  /* font-weight: bold; */
  color: #303133;
}
.right-checkbox {
  margin-right: 10px;
}
.subsidy-table {
  border-top: none;
}
.date-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}
.date-text {
  text-align: right;
  line-height: 1.5;
}
.location-icon {
  color: #909399;
  font-size: 16px;
}
.custom-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-weight: normal;
}
.standard-text {
  color: #ff7d00;
  font-size: 13px;
  margin-bottom: 5px;
}
.input-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.amount-input {
  width: 80px;
}
.amount-input :deep(.el-input__wrapper) {
  padding: 0 10px;
}
.amount-input :deep(.el-input__inner) {
  text-align: center;
  color: #606266;
}
.amount-input.is-disabled :deep(.el-input__inner) {
  color: #c0c4cc;
}
.type-header {
  font-size: 16px;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}
.type-label {
  color: #303133;
  margin-right: 15px;
}
.type-value {
  color: #ff7d00;
  font-size: 16px;
}
.itinerary-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px 15px;
  margin-bottom: 20px;
}
.itinerary-row {
  display: flex;
  align-items: center;
  height: 36px;
}
.row-label {
  color: #606266;
  font-size: 13px;
  width: 60px;
  flex-shrink: 0;
}
.timeline-node {
  width: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  height: 100%;
  flex-shrink: 0;
}
.node-top {
  transform: translateY(-8px);
}
.node-bottom {
  transform: translateY(8px);
}
.donut-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 3px solid #0084ff;
  background: #fff;
  z-index: 2;
  box-sizing: border-box;
}
.line {
  position: absolute;
  width: 2px;
  background-color: #0084ff;
  left: 50%;
  transform: translateX(-50%);
}
.line-down {
  top: 50%;
  bottom: -9px;
}
.line-up {
  top: -9px;
  bottom: 50%;
}
.row-value {
  color: #303133;
  font-size: 13px;
  flex: 1;
}
.blue-bar {
  background-color: #0084ff;
  color: #fff;
  display: flex;
  align-items: center;
  height: 32px;
  padding: 0 10px;
  margin: 10px -5px;
  font-size: 13px;
}
.amount-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px 15px;
}
.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.amount-row:last-child {
  margin-bottom: 0;
}
.amount-label {
  color: #606266;
  font-size: 13px;
}
.amount-value {
  display: flex;
  align-items: baseline;
}
.currency {
  color: #606266;
  font-size: 12px;
  margin-right: 15px;
}
.number {
  color: #ff7d00;
  font-size: 18px;
}
.cell-content {
  text-align: center;
}
:deep(.el-collapse-item__header) {
  font-size: 16px;
  font-weight: normal;
  background-color: #f5f7fa;
  padding: 0 15px 0 20px;
  height: 36px !important;
  line-height: 36px !important;
  min-height: 36px !important;
  position: relative;
  border-bottom: none;
}
:deep(.el-collapse-item__header)::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  background-color: #409eff;
}
:deep(.el-collapse-item) {
  margin-bottom: 15px;
}
:deep(.el-collapse) {
  border-top: none;
  border-bottom: none;
}
:deep(.el-collapse-item__wrap) {
  border-bottom: none;
}
:deep(.el-collapse-item__content) {
  padding-top: 15px;
}
:deep(.el-form-item__label) {
  font-size: 14px;
}
:deep(.el-table) {
  font-size: 14px;
}
.title-base {
  font-size: 16px;
  height: 36px;
}
.base-form :deep(.el-form-item.is-required .el-form-item__label::after),
.itinerary-form-custom :deep(.el-form-item.is-required .el-form-item__label::after) {
  content: '*';
  color: #f56c6c;
  margin-left: 4px;
}
.base-form :deep(.el-form-item.is-required .el-form-item__label::before),
.itinerary-form-custom :deep(.el-form-item.is-required .el-form-item__label::before) {
  display: none;
}
.sub-footer {
  display: flex;
  justify-content: center;
  align-items: center;
}
.refresh-icon {
  color: #409eff;
  cursor: pointer;
  margin: 0 4px;
  font-size: 14px;
}
.refresh-icon:hover {
  opacity: 0.8;
}
.refresh-icon-disabled {
  pointer-events: none;
  opacity: 0.4;
  cursor: not-allowed;
}
.percent-input :deep(.el-input__inner) {
  text-align: right;
}
.right-align-input :deep(.el-input__inner) {
  text-align: right;
}
.percent-input::after {
  content: '%';
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #606266;
  font-size: 14px;
}
.percent-input :deep(.el-input__wrapper) {
  padding-right: 25px;
}
.persistent-blue-btn {
  color: #409eff !important;
  border-color: #c6e2ff !important;
  background-color: #ecf5ff !important;
}
.persistent-blue-btn:hover {
  color: #409eff !important;
  border-color: #c6e2ff !important;
  background-color: #ecf5ff !important;
}
.black-text-alert :deep(.el-alert__title) {
  color: #303133 !important;
}



</style>
