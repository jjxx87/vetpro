package com.vetech.serve.service.impl;

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

@Service
public class ReimbursementServiceImpl extends ServiceImpl<ReimbursementMapper, Reimbursement> implements IReimbursementService {

    @Autowired
    private ReimbursementItineraryMapper itineraryMapper;

    @Autowired
    private ReimbursementSubsidyMapper subsidyMapper;

    @Autowired
    private ReimbursementApportionmentMapper apportionmentMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        deleteNested((String) id);
        return super.removeById(id);
    }

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

    private void deleteNested(String reimbursementId) {
        itineraryMapper.delete(new LambdaQueryWrapper<ReimbursementItinerary>()
                .eq(ReimbursementItinerary::getReimbursementId, reimbursementId));
        subsidyMapper.delete(new LambdaQueryWrapper<ReimbursementSubsidy>()
                .eq(ReimbursementSubsidy::getReimbursementId, reimbursementId));
        apportionmentMapper.delete(new LambdaQueryWrapper<ReimbursementApportionment>()
                .eq(ReimbursementApportionment::getReimbursementId, reimbursementId));
    }
}
