<template>
  <div class="list-container">
    <el-card class="search-card" shadow="never" :body-style="{ padding: '20px 20px 0 20px' }">
      <el-form :model="searchForm" label-width="100px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="报销单号">
              <el-input v-model="searchForm.reimNo" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="标题">
              <el-input v-model="searchForm.title" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="事由">
              <el-input v-model="searchForm.reason" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="费用归属公司">
              <el-select v-model="searchForm.companyId" placeholder="请选择" clearable style="width: 100%">
                <el-option v-for="item in dictStore.companies" :key="item.reimCompanyId" :label="item.reimCompanyName" :value="item.reimCompanyId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="报销部门">
              <el-select v-model="searchForm.departmentId" placeholder="请选择" clearable style="width: 100%">
                <el-option v-for="item in dictStore.departments" :key="item.reimDepartmentId" :label="item.reimDepartmentName" :value="item.reimDepartmentId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="报销人">
              <el-select v-model="searchForm.employeeId" placeholder="请选择" clearable style="width: 100%">
                <el-option v-for="item in dictStore.employees" :key="item.reimburserId" :label="item.reimburserName" :value="item.reimburserId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="业务类型">
              <el-tree-select v-model="searchForm.businessTypeId" :data="dictStore.businessTypeTree" placeholder="请选择" check-strictly clearable style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6" class="text-right" style="padding-bottom: 18px;">
            <el-button plain type="primary" @click="handleAdd">新增</el-button>
            <el-button plain type="primary" @click="handleClear">清除</el-button>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" style="width: 100%" v-loading="loading" header-row-class-name="table-header">
        <el-table-column width="40" align="center" fixed="left">
          <template #header>
            <el-icon color="#409EFF"><Operation /></el-icon>
          </template>
          <template #default="{ $index }">
            {{ (currentPage - 1) * pageSize + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="95" fixed="left">
          <template #default="{ row }">
            <div class="actions">
              <el-button 
                :type="row.status === 1 || row.status === 2 ? 'info' : 'primary'" 
                link 
                @click="handleEdit(row)" 
                :disabled="row.status === 1 || row.status === 2"
              >
                <el-icon :size="16"><EditPen /></el-icon>
              </el-button>
              <el-button type="primary" link @click="handleDelete(row)">
                <el-icon :size="16"><DocumentDelete /></el-icon>
              </el-button>
              <el-dropdown trigger="hover" placement="bottom-start">
                <el-button type="primary" link class="more-trigger">
                  <el-icon :size="16"><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <!-- <el-dropdown-item @click="handleDelete(row)">删除</el-dropdown-item> -->
                    <el-dropdown-item @click="handleVoid(row)" :disabled="row.status === 2">作废</el-dropdown-item>
                    <el-dropdown-item @click="handlePrint(row)">复制</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reimNo" label="报销单号" min-width="160">
          <template #default="{ row }">
            <el-link style="color: #409EFF;" @click="handleEdit(row)">{{ row.id?.slice(0, 18) }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="单据状态" min-width="80">
          <template #default="{ row }">
            <span :class="['status-text', row.status === 2 ? 'status-invalid' : 'status-normal']">
              {{ row.status === 1 ? '已完成' : row.status === 2 ? '已作废' : '草稿' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="单据类型" min-width="100">
          <template #default>日常报销单</template>
        </el-table-column>
        <el-table-column label="报销人" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getEmployeeDisplay(row.reimburserId) }}
          </template>
        </el-table-column>
        <el-table-column label="报销部门" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getDepartmentDisplay(row.reimDepartmentId) }}
          </template>
        </el-table-column>
        <el-table-column label="费用归属公司" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getCompanyDisplay(row.reimCompanyId) }}
          </template>
        </el-table-column>
        <el-table-column label="业务类型" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getBusinessTypeDisplay(row.businessTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="reimbursementTitle" label="报销标题" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link @click="handleEdit(row)" style="color: #409EFF;">{{ row.reimbursementTitle }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="businessTripReason" label="报销事由" min-width="100" show-overflow-tooltip />
        <el-table-column prop="subsidyTotal" label="补助金额" min-width="90" align="right">
          <template #default="{ row }">
            {{ Number(row.subsidyTotal || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="creationTime" label="创建时间" min-width="100">
          <template #default="{ row }">
            {{ row.creationTime?.split(' ')[0] }}
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDictStore } from '../stores/dict'
import { DocumentDelete, EditPen, MoreFilled, Operation } from '@element-plus/icons-vue'
import { getReimbursementList, deleteReimbursement, updateReimbursement } from '../apis/reimbursement'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const dictStore = useDictStore()

const searchForm = ref({
  reimNo: '',
  title: '',
  reason: '',
  companyId: '',
  departmentId: '',
  employeeId: '',
  businessTypeId: ''
})

const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const allData = ref([])

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getReimbursementList()
    allData.value = res || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})

const filteredList = computed(() => {
  const selectedBusinessTypeId = searchForm.value.businessTypeId
  let businessTypeIdSet = null
  if (selectedBusinessTypeId) {
    const childrenByParentId = new Map()
    dictStore.businessTypes.forEach(t => {
      const parentId = t.superiorId
      if (!childrenByParentId.has(parentId)) childrenByParentId.set(parentId, [])
      childrenByParentId.get(parentId).push(t.businessTypeId)
    })

    businessTypeIdSet = new Set()
    const stack = [selectedBusinessTypeId]
    while (stack.length) {
      const id = stack.pop()
      if (businessTypeIdSet.has(id)) continue
      businessTypeIdSet.add(id)
      const children = childrenByParentId.get(id) || []
      children.forEach(childId => stack.push(childId))
    }
  }

  return allData.value.filter(item => {
    let match = true
    if (searchForm.value.reimNo && !item.id?.includes(searchForm.value.reimNo)) match = false
    if (searchForm.value.title && !item.reimbursementTitle?.includes(searchForm.value.title)) match = false
    if (searchForm.value.reason && !item.businessTripReason?.includes(searchForm.value.reason)) match = false
    if (searchForm.value.companyId && item.reimCompanyId !== searchForm.value.companyId) match = false
    if (searchForm.value.departmentId && item.reimDepartmentId !== searchForm.value.departmentId) match = false
    if (searchForm.value.employeeId && item.reimburserId !== searchForm.value.employeeId) match = false
    if (businessTypeIdSet && !businessTypeIdSet.has(item.businessTypeId)) match = false
    return match
  })
})

const tableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

const total = computed(() => filteredList.value.length)

const handleSearch = () => {
  currentPage.value = 1
}

const handleClear = () => {
  searchForm.value = {
    reimNo: '',
    title: '',
    reason: '',
    companyId: '',
    departmentId: '',
    employeeId: '',
    businessTypeId: ''
  }
  handleSearch()
}

const handleAdd = () => {
  router.push('/detail')
}

const handleEdit = (row) => {
  router.push(`/detail/${row.id}`)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该报销单吗?', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteReimbursement(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const handleVoid = (row) => {
  ElMessageBox.confirm('确认作废该报销单吗?', '提示', { type: 'warning' }).then(async () => {
    try {
      await updateReimbursement({ id: row.id, status: 2 })
      ElMessage.success('作废成功')
      fetchList()
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const handlePrint = (row) => {
  ElMessage.warning('打印功能待接入')
}

const handleSizeChange = (val) => {
  pageSize.value = val
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// Display helpers
const getEmployeeDisplay = (id) => {
  const emp = dictStore.employees.find(e => e.reimburserId === id)
  return emp ? `${emp.reimburserName}[${emp.reimburserNo}]` : ''
}

const getDepartmentDisplay = (id) => {
  const dept = dictStore.departments.find(d => d.reimDepartmentId === id)
  return dept ? `[${dept.reimDepartmentNo}]${dept.reimDepartmentName}` : ''
}

const getCompanyDisplay = (id) => {
  const comp = dictStore.companies.find(c => c.reimCompanyId === id)
  return comp ? comp.reimCompanyName : ''
}

const getBusinessTypeDisplay = (id) => {
  const type = dictStore.businessTypes.find(t => t.businessTypeId === id)
  return type ? type.businessTypeName : ''
}

</script>

<style scoped>
.list-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
}
.search-card {
  margin-bottom: 20px;
  border-radius: 4px;
}
.text-right {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.more-trigger {
  padding: 0;
}

.status-text {
  font-size: 13px;
}
.status-normal {
  color: #409EFF;
}
.status-invalid {
  color: #909399;
}

:deep(.table-header th) {
  background-color: #fafafa !important;
  color: #606266;
  font-weight: 500;
  border-bottom: 1px solid #ebeef5;
}
:deep(.el-table) {
  font-size: 13px;
}
:deep(.el-form-item) {
  margin-bottom: 18px;
}
.el-button+.el-button {
    margin-left: 0px;
}
</style>
