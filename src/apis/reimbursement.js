import request from '../utils/request'

// 获取报销单列表
export const getReimbursementList = () => {
  return request({
    url: '/api/reimbursement/list',
    method: 'get'
  })
}

// 获取报销单详情
export const getReimbursementById = (id) => {
  return request({
    url: `/api/reimbursement/${id}`,
    method: 'get'
  })
}

// 新增报销单
export const addReimbursement = (data) => {
  return request({
    url: '/api/reimbursement',
    method: 'post',
    data
  })
}

// 更新报销单
export const updateReimbursement = (data) => {
  return request({
    url: '/api/reimbursement',
    method: 'put',
    data
  })
}

// 删除报销单
export const deleteReimbursement = (id) => {
  return request({
    url: `/api/reimbursement/${id}`,
    method: 'delete'
  })
}

export const exportReimbursementExcel = (id) => {
  return request({
    url: `/api/reimbursement/${id}/export`,
    method: 'get',
    responseType: 'blob'
  })
}
