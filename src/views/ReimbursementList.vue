<template>
  <div class="list-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" label-width="100px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="报销单号">
              <el-input v-model="searchForm.reimNo" placeholder="请输入报销单号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="标题">
              <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="事由">
              <el-input v-model="searchForm.reason" placeholder="请输入事由" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="费用归属公司">
              <el-select v-model="searchForm.companyId" placeholder="请选择" clearable>
                <el-option v-for="item in dictStore.companies" :key="item.reimCompanyId" :label="item.reimCompanyName" :value="item.reimCompanyId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="报销部门">
              <el-select v-model="searchForm.departmentId" placeholder="请选择" clearable>
                <el-option v-for="item in dictStore.departments" :key="item.reimDepartmentId" :label="item.reimDepartmentName" :value="item.reimDepartmentId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="报销人">
              <el-select v-model="searchForm.employeeId" placeholder="请选择" clearable>
                <el-option v-for="item in dictStore.employees" :key="item.reimburserId" :label="item.reimburserName" :value="item.reimburserId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="业务类型">
              <el-tree-select v-model="searchForm.businessTypeId" :data="dictStore.businessTypeTree" placeholder="请选择" check-strictly clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6" class="text-right">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleClear">清除</el-button>
            <el-button type="success" @click="handleAdd">新增</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" style="width: 100%" border>
        <el-table-column label="操作" width="120" fixed="left">
          <template #default="{ row }">
            <div class="actions">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              <el-dropdown trigger="hover" v-if="true">
                <el-button type="primary" link>
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>作废</el-dropdown-item>
                    <el-dropdown-item>打印</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reimNo" label="报销单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleEdit(row)">{{ row.reimNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="单据状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'">
              {{ row.status === 1 ? '已完成' : row.status === 2 ? '已作废' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报销人" width="150">
          <template #default="{ row }">
            {{ getEmployeeDisplay(row.employeeId) }}
          </template>
        </el-table-column>
        <el-table-column label="报销部门" width="180">
          <template #default="{ row }">
            {{ getDepartmentDisplay(row.departmentId) }}
          </template>
        </el-table-column>
        <el-table-column label="费用归属公司" width="200">
          <template #default="{ row }">
            {{ getCompanyDisplay(row.companyId) }}
          </template>
        </el-table-column>
        <el-table-column label="业务类型" width="150">
          <template #default="{ row }">
            {{ getBusinessTypeDisplay(row.businessTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="报销标题" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="handleEdit(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="报销事由" width="200" show-overflow-tooltip />
        <el-table-column prop="totalSubsidy" label="补助金额" width="120" align="right">
          <template #default="{ row }">
            {{ row.totalSubsidy?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150" />
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useDictStore } from '../stores/dict'
import { useReimbursementStore } from '../stores/reimbursement'
import { ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const dictStore = useDictStore()
const reimStore = useReimbursementStore()

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

const filteredList = computed(() => {
  return reimStore.list.filter(item => {
    let match = true
    if (searchForm.value.reimNo && !item.reimNo?.includes(searchForm.value.reimNo)) match = false
    if (searchForm.value.title && !item.title?.includes(searchForm.value.title)) match = false
    if (searchForm.value.reason && !item.reason?.includes(searchForm.value.reason)) match = false
    if (searchForm.value.companyId && item.companyId !== searchForm.value.companyId) match = false
    if (searchForm.value.departmentId && item.departmentId !== searchForm.value.departmentId) match = false
    if (searchForm.value.employeeId && item.employeeId !== searchForm.value.employeeId) match = false
    if (searchForm.value.businessTypeId && item.businessTypeId !== searchForm.value.businessTypeId) match = false
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
  // Mock delete
  const index = reimStore.list.findIndex(item => item.id === row.id)
  if (index !== -1) {
    reimStore.list.splice(index, 1)
  }
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
  return emp ? `${emp.reimburserName}(${emp.reimburserNo})` : ''
}

const getDepartmentDisplay = (id) => {
  const dept = dictStore.departments.find(d => d.reimDepartmentId === id)
  return dept ? `${dept.reimDepartmentName}(${dept.reimDepartmentNo})` : ''
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
}
.text-right {
  text-align: right;
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
</style>