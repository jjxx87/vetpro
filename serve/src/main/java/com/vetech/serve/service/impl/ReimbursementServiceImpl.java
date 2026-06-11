package com.vetech.serve.service.impl;


/**
 * @cn-file
 * 文件：serve/src/main/java/com/vetech/serve/service/impl/ReimbursementServiceImpl.java
 * 说明：后端服务：业务逻辑处理
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vetech.serve.entity.Reimbursement;
import com.vetech.serve.entity.ReimbursementApportionment;
import com.vetech.serve.entity.ReimbursementItinerary;
import com.vetech.serve.entity.ReimbursementSubsidy;
import com.vetech.serve.mapper.ReimbursementApportionmentMapper;
import com.vetech.serve.mapper.ReimbursementItineraryMapper;
import com.vetech.serve.mapper.ReimbursementMapper;
import com.vetech.serve.mapper.ReimbursementSubsidyMapper;
import com.vetech.serve.service.IReimbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * 报销单服务实现，负责主表与关联明细的统一持久化处理。
 */
@Service
public class ReimbursementServiceImpl extends ServiceImpl<ReimbursementMapper, Reimbursement> implements IReimbursementService {

    /**
     * 行程明细 Mapper。
     */
    @Autowired
    private ReimbursementItineraryMapper itineraryMapper;

    /**
     * 补助明细 Mapper。
     */
    @Autowired
    private ReimbursementSubsidyMapper subsidyMapper;

    /**
     * 分摊明细 Mapper。
     */
    @Autowired
    private ReimbursementApportionmentMapper apportionmentMapper;

    /**
     * 保存报销单主表及其关联的明细数据。
     *
     * @param entity 报销单实体
     * @return 是否保存成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Reimbursement entity) {
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        boolean result = super.save(entity);
        saveNested(entity);
        return result;
    }

    /**
     * 更新报销单主表，并先清空后重建关联明细数据。
     *
     * @param entity 报销单实体
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Reimbursement entity) {
        boolean result = super.updateById(entity);
        // Delete old nested
        deleteNested(entity.getId());
        // Save new nested
        saveNested(entity);
        return result;
    }

    /**
     * 删除报销单主表前，先删除关联的行程、补助和分摊明细。
     *
     * @param id 报销单 ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        deleteNested((String) id);
        return super.removeById(id);
    }

    /**
     * 查询报销单详情，并组装关联的明细数据。
     *
     * @param id 报销单 ID
     * @return 带有明细的报销单实体
     */
    @Override
    public Reimbursement getById(Serializable id) {
        Reimbursement reimbursement = super.getById(id);
        if (reimbursement != null) {
            reimbursement.setItineraries(itineraryMapper.selectList(new LambdaQueryWrapper<ReimbursementItinerary>()
                    .eq(ReimbursementItinerary::getReimbursementId, id)));
            reimbursement.setSubsidies(subsidyMapper.selectList(new LambdaQueryWrapper<ReimbursementSubsidy>()
                    .eq(ReimbursementSubsidy::getReimbursementId, id)));
            reimbursement.setApportionments(apportionmentMapper.selectList(new LambdaQueryWrapper<ReimbursementApportionment>()
                    .eq(ReimbursementApportionment::getReimbursementId, id)));
        }
        return reimbursement;
    }

    /**
     * 保存报销单下的全部关联明细。
     *
     * @param entity 报销单实体
     */
    private void saveNested(Reimbursement entity) {
        String id = entity.getId();
        if (entity.getItineraries() != null) {
            for (ReimbursementItinerary item : entity.getItineraries()) {
                item.setReimbursementId(id);
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                itineraryMapper.insert(item);
            }
        }
        if (entity.getSubsidies() != null) {
            for (ReimbursementSubsidy item : entity.getSubsidies()) {
                item.setReimbursementId(id);
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                subsidyMapper.insert(item);
            }
        }
        if (entity.getApportionments() != null) {
            for (ReimbursementApportionment item : entity.getApportionments()) {
                item.setReimbursementId(id);
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                apportionmentMapper.insert(item);
            }
        }
    }

    /**
     * 删除指定报销单下的全部关联明细。
     *
     * @param reimbursementId 报销单 ID
     */
    private void deleteNested(String reimbursementId) {
        itineraryMapper.delete(new LambdaQueryWrapper<ReimbursementItinerary>()
                .eq(ReimbursementItinerary::getReimbursementId, reimbursementId));
        subsidyMapper.delete(new LambdaQueryWrapper<ReimbursementSubsidy>()
                .eq(ReimbursementSubsidy::getReimbursementId, reimbursementId));
        apportionmentMapper.delete(new LambdaQueryWrapper<ReimbursementApportionment>()
                .eq(ReimbursementApportionment::getReimbursementId, reimbursementId));
    }
}
