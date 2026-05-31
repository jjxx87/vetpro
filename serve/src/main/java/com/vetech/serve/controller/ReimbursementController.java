package com.vetech.serve.controller;

import com.vetech.serve.entity.Reimbursement;
import com.vetech.serve.service.IReimbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reimbursement")
public class ReimbursementController {

    @Autowired
    private IReimbursementService reimbursementService;

    @PostMapping
    public boolean save(@RequestBody Reimbursement reimbursement) {
        return reimbursementService.save(reimbursement);
    }

    @GetMapping("/{id}")
    public Reimbursement getById(@PathVariable String id) {
        return reimbursementService.getById(id);
    }

    @GetMapping("/list")
    public List<Reimbursement> list() {
        return reimbursementService.lambdaQuery()
                .orderByDesc(Reimbursement::getCreationTime)
                .list();
    }

    @PutMapping
    public boolean update(@RequestBody Reimbursement reimbursement) {
        return reimbursementService.updateById(reimbursement);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable String id) {
        return reimbursementService.removeById(id);
    }
}
