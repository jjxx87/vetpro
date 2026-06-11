package com.vetech.serve.service;


/**
 * @cn-file
 * 文件：serve/src/main/java/com/vetech/serve/service/IReimbursementService.java
 * 说明：后端服务：业务逻辑处理
 */

import com.baomidou.mybatisplus.extension.service.IService;
import com.vetech.serve.entity.Reimbursement;

/**
 * 报销单服务接口，定义报销主数据的基础业务能力。
 */
public interface IReimbursementService extends IService<Reimbursement> {
}
