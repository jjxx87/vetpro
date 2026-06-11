/**
 * @cn-file
 * @file src/stores/reimbursement.js
 * @desc 状态：全局状态管理（Pinia）
 */

import { defineStore } from 'pinia'

export const useReimbursementStore = defineStore('reimbursement', {
  state: () => ({
    list: []
  }),
  actions: {
    addReimbursement(data) {
      data.id = Date.now().toString()
      data.createTime = new Date().toISOString().split('T')[0]
      this.list.unshift(data)
    },
    updateReimbursement(data) {
      const index = this.list.findIndex(item => item.id === data.id)
      if (index !== -1) {
        this.list[index] = data
      }
    }
  }
})