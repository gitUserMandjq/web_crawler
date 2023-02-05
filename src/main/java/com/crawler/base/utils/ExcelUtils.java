package com.crawler.base.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelUtils {
	static Logger logger = LoggerFactory.getLogger(ExcelUtils.class);
	public static Workbook readExcel(MultipartFile file) throws IOException {
		Workbook wb = null;
		String filePath = file.getOriginalFilename();
		if(filePath.lastIndexOf(".") < 0)
			return wb = null;
		String extString = filePath.substring(filePath.lastIndexOf("."));
    	InputStream is = file.getInputStream();
        if(".xls".equalsIgnoreCase(extString)){
            return wb = new HSSFWorkbook(is);
        }else if(".xlsx".equalsIgnoreCase(extString)){
            return wb = new XSSFWorkbook(is);
        }else{
            return wb = null;
        }
	}
	public static Workbook readExcel(File file) throws IOException {
		Workbook wb = null;
		String filePath = file.getAbsolutePath();
		if(filePath.lastIndexOf(".") < 0)
			return wb = null;
		String extString = filePath.substring(filePath.lastIndexOf("."));
    	InputStream is = new FileInputStream(file);
        if(".xls".equalsIgnoreCase(extString)){
            return wb = new HSSFWorkbook(is);
        }else if(".xlsx".equalsIgnoreCase(extString)){
            return wb = new XSSFWorkbook(is);
        }else{
            return wb = null;
        }
	}
	public static int readExcel(InputStream inputStream, ExcelReadDataDelegated excelReadDataDelegated) throws Exception {
        int totalRows = 0;
        ExcelXlsxReaderWithDefaultHandler excelXlsxReader = new ExcelXlsxReaderWithDefaultHandler(excelReadDataDelegated);
        totalRows = excelXlsxReader.process(inputStream);
        System.out.println("读取的数据总行数：" + totalRows);
        return totalRows;
    }
	public static int readExcel(File file, ExcelReadDataDelegated excelReadDataDelegated) throws Exception {
        int totalRows = 0;
        ExcelXlsxReaderWithDefaultHandler excelXlsxReader = new ExcelXlsxReaderWithDefaultHandler(excelReadDataDelegated);
        totalRows = excelXlsxReader.process(file);
        System.out.println("读取的数据总行数：" + totalRows);
        return totalRows;
    }

	public static String getCellFormatValue(Cell cell) throws ParseException{
        String cellValue = null;
        if(cell!=null){
            //判断cell类型
            switch(cell.getCellType()){
            case Cell.CELL_TYPE_NUMERIC:{
            	//判断cell是否为日期格式
                if(DateUtil.isCellDateFormatted(cell)){
                    //转换为日期格式YYYY-mm-dd
                	Date date = cell.getDateCellValue();
                	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                	cellValue = sdf.format(date);
                }else{
                	String cellstr = cell.toString();
                	cell.setCellType(Cell.CELL_TYPE_STRING);
                	cellValue = cell.getStringCellValue();
                	if(cellValue.indexOf(".") > -1) {
                		cellValue = cellstr;
                	}
                    //数字
                }
                break;
            }
            case Cell.CELL_TYPE_FORMULA:{
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            }
            case Cell.CELL_TYPE_STRING:{
            	cellValue = cell.getStringCellValue();
                break;
            }
            default:
                cellValue = "";
            }
        }else{
            cellValue = "";
        }
        return cellValue;
    }
    public static void exportExcel(HttpServletRequest request, HttpServletResponse response, HSSFWorkbook wb, String fileName) throws IOException {
        OutputStream os = response.getOutputStream();
        String header = request.getHeader("User-Agent").toUpperCase();
        if (header.contains("MSIE") || header.contains("TRIDENT") || header.contains("EDGE")) {
            fileName = URLEncoder.encode(fileName, "utf-8");
            fileName = fileName.replace("+", "%20");    //IE下载文件名空格变+号问题
        } else {
            fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel;charset=utf-8");// 设置contentType为excel格式
        response.setHeader("Content-Disposition", "Attachment;Filename=" + fileName + ".xlsx");
        wb.write(os);
        os.close();
    }
}
