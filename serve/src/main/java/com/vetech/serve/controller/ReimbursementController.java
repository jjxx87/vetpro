package com.vetech.serve.controller;

import com.vetech.serve.entity.Reimbursement;
import com.vetech.serve.entity.ReimbursementApportionment;
import com.vetech.serve.entity.ReimbursementItinerary;
import com.vetech.serve.entity.ReimbursementSubsidy;
import com.vetech.serve.service.IReimbursementService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public List<Reimbursement> list(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String reimbursementTitle,
            @RequestParam(required = false) String businessTripReason,
            @RequestParam(required = false) String reimCompanyId,
            @RequestParam(required = false) String reimDepartmentId,
            @RequestParam(required = false) String reimburserId,
            @RequestParam(required = false) List<String> businessTypeIds) {
        return reimbursementService.lambdaQuery()
                .like(StringUtils.hasText(id), Reimbursement::getId, id)
                .like(StringUtils.hasText(reimbursementTitle), Reimbursement::getReimbursementTitle, reimbursementTitle)
                .like(StringUtils.hasText(businessTripReason), Reimbursement::getBusinessTripReason, businessTripReason)
                .eq(StringUtils.hasText(reimCompanyId), Reimbursement::getReimCompanyId, reimCompanyId)
                .eq(StringUtils.hasText(reimDepartmentId), Reimbursement::getReimDepartmentId, reimDepartmentId)
                .eq(StringUtils.hasText(reimburserId), Reimbursement::getReimburserId, reimburserId)
                .in(businessTypeIds != null && !businessTypeIds.isEmpty(), Reimbursement::getBusinessTypeId, businessTypeIds)
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

    @GetMapping("/{id}/export")
    public void export(@PathVariable String id, HttpServletResponse response) throws IOException {
        Reimbursement reimbursement = reimbursementService.getById(id);
        if (reimbursement == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String filename = "reimbursement_" + id + ".xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet main = workbook.createSheet("Main");
            int r = 0;
            r = writeKv(main, r, "ID", reimbursement.getId());
            r = writeKv(main, r, "创建时间", reimbursement.getCreationTime());
            r = writeKv(main, r, "标题", reimbursement.getReimbursementTitle());
            r = writeKv(main, r, "报销人ID", reimbursement.getReimburserId());
            r = writeKv(main, r, "报销人姓名", reimbursement.getReimburserName());
            r = writeKv(main, r, "报销人工号", reimbursement.getReimburserNo());
            r = writeKv(main, r, "部门ID", reimbursement.getReimDepartmentId());
            r = writeKv(main, r, "部门名称", reimbursement.getReimDepartmentName());
            r = writeKv(main, r, "公司ID", reimbursement.getReimCompanyId());
            r = writeKv(main, r, "公司名称", reimbursement.getReimCompanyName());
            r = writeKv(main, r, "业务类型ID", reimbursement.getBusinessTypeId());
            r = writeKv(main, r, "业务类型名称", reimbursement.getBusinessTypeName());
            r = writeKv(main, r, "出差事由", reimbursement.getBusinessTripReason());
            r = writeKv(main, r, "补助总金额", reimbursement.getSubsidyTotal());
            r = writeKv(main, r, "餐费补助", reimbursement.getMealAllowance());
            r = writeKv(main, r, "交通补助", reimbursement.getTransportationAllowance());
            r = writeKv(main, r, "通讯补助", reimbursement.getPhoneAllowance());
            r = writeKv(main, r, "备注", reimbursement.getRemarks());
            r = writeKv(main, r, "状态", reimbursement.getStatus() == null ? "" : String.valueOf(reimbursement.getStatus()));

            Sheet itinerarySheet = workbook.createSheet("Itineraries");
            writeItineraries(itinerarySheet, reimbursement.getItineraries());

            Sheet subsidySheet = workbook.createSheet("Subsidies");
            Sheet calendarSheet = workbook.createSheet("Calendar");
            writeSubsidiesAndCalendar(subsidySheet, calendarSheet, reimbursement.getSubsidies());

            Sheet apportionSheet = workbook.createSheet("Apportionments");
            writeApportionments(apportionSheet, reimbursement.getApportionments());

            workbook.write(response.getOutputStream());
            response.flushBuffer();
        }
    }

    private int writeKv(Sheet sheet, int rowIndex, String k, String v) {
        Row row = sheet.createRow(rowIndex);
        Cell c0 = row.createCell(0);
        c0.setCellValue(k);
        Cell c1 = row.createCell(1);
        c1.setCellValue(v == null ? "" : v);
        return rowIndex + 1;
    }

    private void writeItineraries(Sheet sheet, List<ReimbursementItinerary> list) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("employeeId");
        header.createCell(1).setCellValue("startCity");
        header.createCell(2).setCellValue("endCity");
        header.createCell(3).setCellValue("startDate");
        header.createCell(4).setCellValue("endDate");
        header.createCell(5).setCellValue("reason");

        if (list == null) return;
        int r = 1;
        for (ReimbursementItinerary it : list) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(nvl(it.getEmployeeId()));
            row.createCell(1).setCellValue(nvl(it.getStartCity()));
            row.createCell(2).setCellValue(nvl(it.getEndCity()));
            row.createCell(3).setCellValue(nvl(it.getStartDate()));
            row.createCell(4).setCellValue(nvl(it.getEndDate()));
            row.createCell(5).setCellValue(nvl(it.getReason()));
        }
    }

    private void writeApportionments(Sheet sheet, List<ReimbursementApportionment> list) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("companyId");
        header.createCell(1).setCellValue("projectId");
        header.createCell(2).setCellValue("percent");
        header.createCell(3).setCellValue("amount");

        if (list == null) return;
        int r = 1;
        for (ReimbursementApportionment a : list) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(nvl(a.getCompanyId()));
            row.createCell(1).setCellValue(nvl(a.getProjectId()));
            row.createCell(2).setCellValue(a.getPercent() == null ? "" : a.getPercent().toPlainString());
            row.createCell(3).setCellValue(a.getAmount() == null ? "" : a.getAmount().toPlainString());
        }
    }

    private void writeSubsidiesAndCalendar(Sheet subsidySheet, Sheet calendarSheet, List<ReimbursementSubsidy> subsidies) {
        Row sHeader = subsidySheet.createRow(0);
        sHeader.createCell(0).setCellValue("employeeId");
        sHeader.createCell(1).setCellValue("startDate");
        sHeader.createCell(2).setCellValue("endDate");
        sHeader.createCell(3).setCellValue("startCity");
        sHeader.createCell(4).setCellValue("endCity");
        sHeader.createCell(5).setCellValue("days");
        sHeader.createCell(6).setCellValue("standardTotal");
        sHeader.createCell(7).setCellValue("subsidyTotal");

        Row cHeader = calendarSheet.createRow(0);
        cHeader.createCell(0).setCellValue("subsidyIndex");
        cHeader.createCell(1).setCellValue("date");
        cHeader.createCell(2).setCellValue("weekday");
        cHeader.createCell(3).setCellValue("city");
        cHeader.createCell(4).setCellValue("mealSelected");
        cHeader.createCell(5).setCellValue("mealStandard");
        cHeader.createCell(6).setCellValue("mealAmount");
        cHeader.createCell(7).setCellValue("trafficSelected");
        cHeader.createCell(8).setCellValue("trafficStandard");
        cHeader.createCell(9).setCellValue("trafficAmount");
        cHeader.createCell(10).setCellValue("commSelected");
        cHeader.createCell(11).setCellValue("commStandard");
        cHeader.createCell(12).setCellValue("commAmount");

        if (subsidies == null) return;

        int sRowIndex = 1;
        int cRowIndex = 1;
        for (int i = 0; i < subsidies.size(); i++) {
            ReimbursementSubsidy s = subsidies.get(i);
            List<Map<String, Object>> calendar = s.getCalendar() == null ? new ArrayList<>() : s.getCalendar();

            BigDecimal standardTotal = BigDecimal.ZERO;
            BigDecimal subsidyTotal = BigDecimal.ZERO;

            for (Map<String, Object> cal : calendar) {
                BigDecimal mealStd = num(cal.get("mealStandard"));
                BigDecimal trafficStd = num(cal.get("trafficStandard"));
                BigDecimal commStd = num(cal.get("commStandard"));
                standardTotal = standardTotal.add(mealStd).add(trafficStd).add(commStd);

                boolean mealSelected = bool(cal.get("mealSelected"));
                boolean trafficSelected = bool(cal.get("trafficSelected"));
                boolean commSelected = bool(cal.get("commSelected"));
                if (mealSelected) subsidyTotal = subsidyTotal.add(num(cal.get("mealAmount")));
                if (trafficSelected) subsidyTotal = subsidyTotal.add(num(cal.get("trafficAmount")));
                if (commSelected) subsidyTotal = subsidyTotal.add(num(cal.get("commAmount")));

                Row cr = calendarSheet.createRow(cRowIndex++);
                cr.createCell(0).setCellValue(i + 1);
                cr.createCell(1).setCellValue(nvl(str(cal.get("date"))));
                cr.createCell(2).setCellValue(nvl(str(cal.get("weekday"))));
                cr.createCell(3).setCellValue(nvl(str(cal.get("city"))));
                cr.createCell(4).setCellValue(mealSelected ? "true" : "false");
                cr.createCell(5).setCellValue(mealStd.toPlainString());
                cr.createCell(6).setCellValue(num(cal.get("mealAmount")).toPlainString());
                cr.createCell(7).setCellValue(trafficSelected ? "true" : "false");
                cr.createCell(8).setCellValue(trafficStd.toPlainString());
                cr.createCell(9).setCellValue(num(cal.get("trafficAmount")).toPlainString());
                cr.createCell(10).setCellValue(commSelected ? "true" : "false");
                cr.createCell(11).setCellValue(commStd.toPlainString());
                cr.createCell(12).setCellValue(num(cal.get("commAmount")).toPlainString());
            }

            Row sr = subsidySheet.createRow(sRowIndex++);
            sr.createCell(0).setCellValue(nvl(s.getEmployeeId()));
            sr.createCell(1).setCellValue(nvl(s.getStartDate()));
            sr.createCell(2).setCellValue(nvl(s.getEndDate()));
            sr.createCell(3).setCellValue(nvl(s.getStartCity()));
            sr.createCell(4).setCellValue(nvl(s.getEndCity()));
            sr.createCell(5).setCellValue(s.getDays() == null ? String.valueOf(calendar.size()) : String.valueOf(s.getDays()));
            sr.createCell(6).setCellValue(standardTotal.toPlainString());
            sr.createCell(7).setCellValue(subsidyTotal.toPlainString());
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String str(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private BigDecimal num(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean bool(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        return "true".equalsIgnoreCase(String.valueOf(v)) || "1".equals(String.valueOf(v));
    }
}
