package com.vetech.serve.controller;


/**
 * @cn-file
 * 文件：serve/src/main/java/com/vetech/serve/controller/ReimbursementController.java
 * 说明：后端控制器：提供 HTTP 接口
 */

import com.vetech.serve.entity.Reimbursement;
import com.vetech.serve.entity.ReimbursementApportionment;
import com.vetech.serve.entity.ReimbursementItinerary;
import com.vetech.serve.entity.ReimbursementSubsidy;
import com.vetech.serve.service.IReimbursementService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

/**
 * 报销单控制器，提供报销单的增删改查与导出接口。
 */
@RestController
@RequestMapping("/api/reimbursement")
public class ReimbursementController {

    /**
     * 报销单业务服务。
     */
    @Autowired
    private IReimbursementService reimbursementService;

    /**
     * 新增报销单。
     *
     * @param reimbursement 报销单数据
     * @return 是否保存成功
     */
    @PostMapping
    public boolean save(@RequestBody Reimbursement reimbursement) {
        return reimbursementService.save(reimbursement);
    }

    /**
     * 根据主键查询报销单详情。
     *
     * @param id 报销单 ID
     * @return 报销单详情
     */
    @GetMapping("/{id}")
    public Reimbursement getById(@PathVariable String id) {
        return reimbursementService.getById(id);
    }

    /**
     * 分页查询报销单列表，并按条件执行模糊或精确筛选。
     *
     * @param current 当前页码
     * @param size 每页条数
     * @param id 报销单 ID
     * @param reimbursementTitle 报销标题
     * @param businessTripReason 出差事由
     * @param reimCompanyId 费用归属公司 ID
     * @param reimDepartmentId 报销部门 ID
     * @param reimburserId 报销人 ID
     * @param businessTypeIds 业务类型 ID 集合
     * @return 分页后的报销单列表
     */
    @GetMapping("/list")
    public IPage<Reimbursement> list(
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String reimbursementTitle,
            @RequestParam(required = false) String businessTripReason,
            @RequestParam(required = false) String reimCompanyId,
            @RequestParam(required = false) String reimDepartmentId,
            @RequestParam(required = false) String reimburserId,
            @RequestParam(required = false) List<String> businessTypeIds) {
        Page<Reimbursement> page = new Page<>(current, size);
        return reimbursementService.lambdaQuery()
                .like(StringUtils.hasText(id), Reimbursement::getId, id)
                .like(StringUtils.hasText(reimbursementTitle), Reimbursement::getReimbursementTitle, reimbursementTitle)
                .like(StringUtils.hasText(businessTripReason), Reimbursement::getBusinessTripReason, businessTripReason)
                .eq(StringUtils.hasText(reimCompanyId), Reimbursement::getReimCompanyId, reimCompanyId)
                .eq(StringUtils.hasText(reimDepartmentId), Reimbursement::getReimDepartmentId, reimDepartmentId)
                .eq(StringUtils.hasText(reimburserId), Reimbursement::getReimburserId, reimburserId)
                .in(businessTypeIds != null && !businessTypeIds.isEmpty(), Reimbursement::getBusinessTypeId, businessTypeIds)
                .orderByDesc(Reimbursement::getCreationTime)
                .page(page);
    }

    /**
     * 更新报销单主数据及其关联明细。
     *
     * @param reimbursement 报销单数据
     * @return 是否更新成功
     */
    @PutMapping
    public boolean update(@RequestBody Reimbursement reimbursement) {
        return reimbursementService.updateById(reimbursement);
    }

    /**
     * 删除指定报销单。
     *
     * @param id 报销单 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable String id) {
        return reimbursementService.removeById(id);
    }

    /**
     * 导出指定报销单的 Excel 文件，包含主表、行程、补助和分摊数据。
     *
     * @param id 报销单 ID
     * @param response HTTP 响应对象
     * @throws IOException 输出流写入异常
     */
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

    /**
     * 在工作表中按“键值对”形式写入一行数据。
     *
     * @param sheet 目标工作表
     * @param rowIndex 当前行号
     * @param k 键
     * @param v 值
     * @return 下一行行号
     */
    private int writeKv(Sheet sheet, int rowIndex, String k, String v) {
        Row row = sheet.createRow(rowIndex);
        Cell c0 = row.createCell(0);
        c0.setCellValue(k);
        Cell c1 = row.createCell(1);
        c1.setCellValue(v == null ? "" : v);
        return rowIndex + 1;
    }

    /**
     * 将行程明细写入指定工作表。
     *
     * @param sheet 行程工作表
     * @param list 行程列表
     */
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

    /**
     * 将费用分摊明细写入指定工作表。
     *
     * @param sheet 分摊工作表
     * @param list 分摊列表
     */
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

    /**
     * 将补助汇总和逐日补助日历分别写入两个工作表。
     *
     * @param subsidySheet 补助汇总工作表
     * @param calendarSheet 补助日历工作表
     * @param subsidies 补助列表
     */
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

    /**
     * 空值保护，避免字符串为空指针。
     *
     * @param s 原始字符串
     * @return 非空字符串
     */
    private String nvl(String s) {
        return s == null ? "" : s;
    }

    /**
     * 将任意对象安全转换为字符串。
     *
     * @param v 原始对象
     * @return 转换后的字符串
     */
    private String str(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    /**
     * 将任意对象安全转换为数值，无法转换时返回零。
     *
     * @param v 原始对象
     * @return 转换后的 BigDecimal
     */
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

    /**
     * 将任意对象安全转换为布尔值。
     *
     * @param v 原始对象
     * @return 转换后的布尔结果
     */
    private boolean bool(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        return "true".equalsIgnoreCase(String.valueOf(v)) || "1".equals(String.valueOf(v));
    }
}
