package com.vetech.serve.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vetech.serve.entity.Reimbursement;
import com.vetech.serve.mapper.ReimbursementMapper;
import com.vetech.serve.service.IReimbursementService;
import org.springframework.stereotype.Service;

@Service
public class ReimbursementServiceImpl extends ServiceImpl<ReimbursementMapper, Reimbursement> implements IReimbursementService {
}