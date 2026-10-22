package com.example.demo.service;

import com.example.demo.mapper.TieMapper;
import com.example.demo.model.Tie;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    @Autowired
    private TieMapper tieMapper;

    /**
     * 导出最新 10 个帖子到 Excel 文件
     * @return 文件路径
     */
    public String exportLatestTies() throws IOException {
        List<Tie> latestTies = tieMapper.getLatest10Ties();
        
        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("最新帖子");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "发帖者名字", "帖子标题", "发帖时间", "是否有附件"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 1;
            for (int i = 0; i < latestTies.size(); i++) {
                Tie tie = latestTies.get(i);
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(i + 1); // 序号
                row.createCell(1).setCellValue(tie.getUserName()); // 发帖者名字
                row.createCell(2).setCellValue(tie.getTieTitle()); // 帖子标题
                row.createCell(3).setCellValue(formatDateTime(tie.getTieCreateTime())); // 发帖时间
                row.createCell(4).setCellValue(hasAttachment(tie.getTieImage()) ? "有" : "无"); // 是否有附件
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 生成文件名（带时间戳）
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "latest_ties_" + timestamp + ".xlsx";
            String filePath = "src/main/resources/exports/" + fileName;
            
            // 确保目录存在
            java.io.File dir = new java.io.File("src/main/resources/exports");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            
            System.out.println("Excel 文件已生成：" + filePath);
            return filePath;
        }
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * 判断是否有附件
     */
    private boolean hasAttachment(String tieImage) {
        return tieImage != null && !tieImage.trim().isEmpty();
    }
}
