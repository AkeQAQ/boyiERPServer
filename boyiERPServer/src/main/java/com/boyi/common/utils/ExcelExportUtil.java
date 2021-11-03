package com.boyi.common.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.boyi.entity.RepositoryBuyinDocument;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
* 导出
**/
@Data
public class ExcelExportUtil<T> {

    private int rowIndex;
    private int styleIndex;
    private String templatePath;
    private Class clazz;
    private Field fields[];

    public ExcelExportUtil(Class clazz,int rowIndex,int styleIndex) {
        this.clazz = clazz;
        this.rowIndex = rowIndex;
        this.styleIndex = styleIndex;

        Field[] supperField = clazz.getSuperclass().getDeclaredFields();
        this.fields = (Field[]) ArrayUtils.addAll(supperField, clazz.getDeclaredFields());
    }

    /**
     * 基于注解导出
     */
    public void export(String addPrefixField,String addPreContent,HttpServletResponse response, InputStream is, List<T> objs, String fileName, Map<Integer,String> statusMap) throws Exception {

        XSSFWorkbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);

        CellStyle[] styles = getTemplateStyles(sheet.getRow(styleIndex));

        AtomicInteger datasAi = new AtomicInteger(rowIndex);
        for (T t : objs) {
            Row row = sheet.createRow(datasAi.getAndIncrement());
            for(int i=0;i<styles.length;i++) {
                Cell cell = row.createCell(i);
                cell.setCellStyle(styles[i]);
                for (Field field : fields) {
                    if(field.isAnnotationPresent(ExcelAttribute.class)){
                        field.setAccessible(true);
                        ExcelAttribute ea = field.getAnnotation(ExcelAttribute.class);
                        if(i == ea.sort()) { //列序号
                            try{
                                Object f = field.get(t);
                                if("status".equals(field.getName())){
                                    cell.setCellValue(statusMap.get(Integer.valueOf(field.get(t).toString())));
                                }
                                else{
                                    cell.setCellValue(f ==null ? "":field.get(t).toString());
                                }
                                if(StringUtils.isNotBlank(addPrefixField) && field.getName().equals(addPrefixField)){
                                    cell.setCellValue(addPreContent+cell.getStringCellValue());
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment;filename=" + new String(fileName.getBytes("ISO8859-1")));
        response.setHeader("filename", fileName);
        workbook.write(response.getOutputStream());
    }


    /**
     * 基于注解导出
     */
    public void export(FileOutputStream fos, InputStream is, List<T> objs, String fileName) throws Exception {

        XSSFWorkbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);

        CellStyle[] styles = getTemplateStyles(sheet.getRow(styleIndex));

        AtomicInteger datasAi = new AtomicInteger(rowIndex);
        for (T t : objs) {
            Row row = sheet.createRow(datasAi.getAndIncrement());
            for(int i=0;i<styles.length;i++) {
                Cell cell = row.createCell(i);
                cell.setCellStyle(styles[i]);
                for (Field field : fields) {
                    if(field.isAnnotationPresent(ExcelAttribute.class)){
                        field.setAccessible(true);
                        ExcelAttribute ea = field.getAnnotation(ExcelAttribute.class);
                        if(i == ea.sort()) { //列序号
                            cell.setCellValue(field.get(t).toString());
                        }
                    }
                }
            }
        }
        fileName = URLEncoder.encode(fileName, "UTF-8");
        workbook.write(fos);
    }

    public CellStyle[] getTemplateStyles(Row row) {
        CellStyle [] styles = new CellStyle[row.getLastCellNum()];
        for(int i=0;i<row.getLastCellNum();i++) {
            styles[i] = row.getCell(i).getCellStyle();
        }
        return styles;
    }

    public static void main(String[] args)throws Exception {
        FileInputStream fis = new FileInputStream(new File("D:/demo.xlsx"));
        FileOutputStream fos = new FileOutputStream(new File("D:/out.xlsx"));

        ArrayList<RepositoryBuyinDocument> list = new ArrayList<>();
        RepositoryBuyinDocument r = new RepositoryBuyinDocument();
        r.setMaterialName("测试");
        list.add(r);
        new ExcelExportUtil(RepositoryBuyinDocument.class,1,0).export(fos,fis,list,"报表.xlsx");

    }
}