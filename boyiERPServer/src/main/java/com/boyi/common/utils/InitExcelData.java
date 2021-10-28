package com.boyi.common.utils;

import com.boyi.common.constant.DBConstant;
import com.boyi.entity.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

public class InitExcelData {

    private static Map<String, String> changeMap;

    public static void main(String[] args) throws Exception{

        // 1. 采购价目 ( 数据库手动更新一下状态 ： update base_supplier_material set status = 0)
       /* Map<String, String> fieldDYMap = new HashMap<>();//excel 字段和数据库字段的映射关系
        fieldDYMap.put("供应商","supplier_id");// 需要通过关联查询供应商ID
        fieldDYMap.put("物料编码","material_id");
        fieldDYMap.put("单价","price");
        fieldDYMap.put("生效日期","start_date");
        fieldDYMap.put("失效日期","end_date");
        fieldDYMap.put("编码","id");
        fieldDYMap.put("备注","comment");

        Boolean isGroup =  false;
        String groupName="编码"; // excel 的哪列是编码，需要分割出分组编码.


        Map<String, String> fieldDYDBMap = new HashMap<>();//数据库字段 和 entity字段和的映射关系
        fieldDYDBMap.put("supplier_id","supplierId");
        fieldDYDBMap.put("material_id","materialId");
        fieldDYDBMap.put("price","price");
        fieldDYDBMap.put("start_date","startDate");
        fieldDYDBMap.put("end_date","endDate");
        fieldDYDBMap.put("id","id");
        fieldDYDBMap.put("comment","comment");

        Map<String, String> entityAndDBMap = new HashMap<>();//entity字段 和 数据库 字段和的映射关系
        entityAndDBMap.put("supplierId","supplier_id");
        entityAndDBMap.put("materialId","material_id");
        entityAndDBMap.put("price","price");
        entityAndDBMap.put("startDate","start_date");
        entityAndDBMap.put("endDate","end_date");
        entityAndDBMap.put("id","id");
        entityAndDBMap.put("comment","comment");

        String tableName = "base_supplier_material";

        String filePath="C:\\Users\\Ake\\Downloads\\采购价目表_2021100310302798_100041.xlsx";


        Class<BaseSupplierMaterial> clazz = BaseSupplierMaterial.class;

        Boolean isChangeFieldContent =  true;
        String fieldName="供应商"; // 修改对应的内容

        HashSet<String> outZero = new HashSet<>(); // 去除编号最后有0开头的。
        outZero.add("materialId");
*/
        // 2. 供应商管理
        /*Map<String, String> fieldDYMap = new HashMap<>();//excel 字段和数据库字段的映射关系
        fieldDYMap.put("编码","id");
        fieldDYMap.put("名称","name");
        fieldDYMap.put("供应商分组","group_name");

        Boolean isGroup =  true;
        String groupName="编码"; // excel 的哪列是编码，需要分割出分组编码.

        Map<String, String> fieldDYDBMap = new HashMap<>();//数据库字段 和 entity字段和的映射关系
        fieldDYDBMap.put("id","id");
        fieldDYDBMap.put("name","name");
        fieldDYDBMap.put("group_name","groupName");
        fieldDYDBMap.put("group_code","groupCode");
        fieldDYDBMap.put("sub_id","subId");

        Map<String, String> entityAndDBMap = new HashMap<>();//entity字段 和 数据库 字段和的映射关系
        entityAndDBMap.put("id","id");
        entityAndDBMap.put("name","name");
        entityAndDBMap.put("groupName","group_name");
        entityAndDBMap.put("groupCode","group_code");
        entityAndDBMap.put("subId","sub_id");

        String tableName = "base_supplier";

        String filePath="C:\\Users\\Ake\\Downloads\\供应商_2021100309382710_100044.xlsx";

        Class<BaseSupplier> clazz = BaseSupplier.class;

        Boolean isChangeFieldContent =  false;
        String fieldName="供应商"; // 修改对应的内容

        HashSet<String> outZero = new HashSet<>();
        List list = getEntity(outZero,isChangeFieldContent,fieldName,isGroup,groupName,filePath, clazz, fieldDYMap, fieldDYDBMap);
*/
        // 3. 物料管理
        /*Map<String, String> fieldDYMap = new HashMap<>();//excel 字段和数据库字段的映射关系
        fieldDYMap.put("编码","id");
        fieldDYMap.put("名称","name");
        fieldDYMap.put("规格型号","specs");
        fieldDYMap.put("基本单位","unit");

        Boolean isGroup =  true;
        String groupName="编码"; // excel 的哪列是编码，需要分割出分组编码.

        Map<String, String> fieldDYDBMap = new HashMap<>();//数据库字段 和 entity字段和的映射关系
        fieldDYDBMap.put("id","id");
        fieldDYDBMap.put("name","name");
        fieldDYDBMap.put("group_code","groupCode");
        fieldDYDBMap.put("sub_id","subId");
        fieldDYDBMap.put("specs","specs");
        fieldDYDBMap.put("unit","unit");

        Map<String, String> entityAndDBMap = new HashMap<>();//entity字段 和 数据库 字段和的映射关系
        entityAndDBMap.put("id","id");
        entityAndDBMap.put("name","name");
        entityAndDBMap.put("groupCode","group_code");
        entityAndDBMap.put("subId","sub_id");
        entityAndDBMap.put("specs","specs");
        entityAndDBMap.put("unit","unit");

        String tableName = "base_material";

        String filePath="C:\\Users\\Ake\\Downloads\\物料_2021100309592000_100044.xlsx";

        Class<BaseMaterial> clazz = BaseMaterial.class;

        Boolean isChangeFieldContent =  false;
        String fieldName="供应商"; // 修改对应的内容

        HashSet<String> outZero = new HashSet<>();*/


        // 4. 库存
        Map<String, String> fieldDYMap = new HashMap<>();//excel 字段和数据库字段的映射关系
        fieldDYMap.put("物料编码","material_id");
        fieldDYMap.put("库存量(主单位)","num");

        Boolean isGroup =  false;
        String groupName="编码"; // excel 的哪列是编码，需要分割出分组编码.

        Map<String, String> fieldDYDBMap = new HashMap<>();//数据库字段 和 entity字段和的映射关系
        fieldDYDBMap.put("material_id","materialId");
        fieldDYDBMap.put("num","num");

        Map<String, String> entityAndDBMap = new HashMap<>();//entity字段 和 数据库 字段和的映射关系
        entityAndDBMap.put("materialId","material_id");
        entityAndDBMap.put("num","num");

        String tableName = "repository_stock";

        String filePath="C:\\Users\\Ake\\Downloads\\即时库存汇总数据查询_2021100310581319_100044.xlsx";

        Class<RepositoryStock> clazz = RepositoryStock.class;

        Boolean isChangeFieldContent =  false;
        String fieldName="供应商"; // 修改对应的内容

        HashSet<String> outZero = new HashSet<>();
        outZero.add("materialId");

        List list = getEntity(outZero,isChangeFieldContent,fieldName,isGroup,groupName,filePath, clazz, fieldDYMap, fieldDYDBMap);

        List<String> sqls = getSqls(tableName,list, clazz, entityAndDBMap);

        long start = System.currentTimeMillis();
        toDB(sqls);
        long end = System.currentTimeMillis();
        System.out.println("插入数据库耗时:"+(end-start)+"ms");
    }

    private static Connection getConn() {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://127.0.0.1:3306/boyi?useUnicode=true&useSSL=false&characterEncoding=utf8";
        String username = "root";
        String password = "root";
        Connection conn = null;
        try {
            Class.forName(driver); //classLoader,加载对应驱动
            conn = (Connection) DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    private static void toDB(List<String> sqls)throws Exception{

        Connection con = getConn();
        con.setAutoCommit(false);
        Statement stmt = con.createStatement();
        for(int i=0; i<sqls.size(); i++){
            stmt.addBatch(sqls.get(i));
            // 1w条记录插入一次
            if (i % 10000 == 0){
                stmt.executeBatch();
                con.commit();
            }
        }
        // 最后插入不足1w条的数据
        stmt.executeBatch();
        con.commit();
    }

    private static List<String> getSqls(String tableName,List list,Class clazz, Map<String, String> entityAndDBMap)throws Exception {

        Field[] fields = clazz.getDeclaredFields();

        List<String> insertSqls = new ArrayList<>();

        for (Object obj : list){
            StringBuilder dbSqls = new StringBuilder();
            StringBuilder valueSqls = new StringBuilder();


            HashMap<String, Object> values = new HashMap<>();

            for (Field field :fields){
                String fieldName = field.getName();
                field.setAccessible(true);
                Object value = field.get(obj);
                String dbName = entityAndDBMap.get(fieldName);
                if(dbName!=null && !dbName.isEmpty() && value != null){
                    values.put(dbName,value);
                    dbSqls.append(dbName).append(",");
                    valueSqls.append("'").append(value).append("'").append(",");
                }
            }
            dbSqls.deleteCharAt(dbSqls.length()-1);
            valueSqls.deleteCharAt(valueSqls.length()-1);

            String sql = "insert into "+tableName+" ("+dbSqls+")values("+valueSqls+")";

            System.out.println("一个插入sql:"+sql);
            insertSqls.add(sql);
        }

        return insertSqls;
    }

    public static List getEntity(HashSet<String> outZero, Boolean isChangeFieldContent, String fieldName, Boolean isGroup, String groupName, String filePath, Class clazz, Map<String, String> fieldDYMap, Map<String, String> fieldDYDBMap)throws  Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Map<String,String> changeMap = null;
        if(isChangeFieldContent){
            // 查询对应修改字段的，映射map
            changeMap = getChangeMap();
        }

        Map<Integer, String> excelFieldIndexMap = new HashMap<>();//excel 字段和下标的对应关系,比如：1:编码
        Field[] fields = clazz.getDeclaredFields();


        List<Object> list = new ArrayList<Object>();
        try(FileInputStream fis = new FileInputStream(filePath);)
        {
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);
            // 不准确
            int rowLength = sheet.getLastRowNum();

            System.out.println(sheet.getLastRowNum());


            outRow:for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                System.out.println(row.getLastCellNum());
                Object entity = (Object) clazz.newInstance();

                innerJ:for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if(cell==null){
                        System.out.println("cell is null"+cell);
                        continue innerJ;
                    }
                    CellType cellType = cell.getCellType();
                    String content = "";
                    if(cellType.equals(CellType.NUMERIC)) {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            Date dt = DateUtil.getJavaDate(cell.getNumericCellValue());
                            content = sdf.format(dt);
                        } else {
                            // 防止数值变成科学计数法
                            String strCell = "";
                            Double num = cell.getNumericCellValue();
                            BigDecimal bd = new BigDecimal(num.toString());
                            if (bd != null) {
                                strCell = bd.toPlainString();
                            }
                            // 去除 浮点型 自动加的 .0
                            if (strCell.endsWith(".0")) {
                                strCell = strCell.substring(0, strCell.indexOf("."));
                            }
                            content = strCell;
                        }
                    }else{
                        content = cell.getStringCellValue();
                    }

                    if(rowNum == 0){
                        excelFieldIndexMap.put(j,content);
                    }else{
                        String theIndexContent = excelFieldIndexMap.get(j);
                        String group_code = null;
                        String sub_id = "";
                        boolean group = isGroup && theIndexContent.equals(groupName);
                        // 判断该列是否为分组列。
                        if(group){
                            if(content==null || content.isEmpty()){
                                System.out.println("分组列的内容是null。"+row);
                                continue outRow;
                            }
                            // 是分组列，则分出子分组编码.
                            int index = content.lastIndexOf('.');
                            group_code = content.substring(0, index);
                            String subStr = content.substring(index + 1);

                            sub_id = outFrontZero(subStr);

                        }

                        // 假如是修改列，则替换对应内容
                        if(theIndexContent.equals(fieldName)){
                            String changeContent = changeMap.get(content);
                            if(changeContent == null || changeContent.isEmpty()){
                                throw new Exception("没有找到对应变更字段内容:"+content);
                            }
                            content = changeContent;
                        }

                        String theDBCode = fieldDYMap.get(theIndexContent);

                        String entityName = fieldDYDBMap.get(theDBCode);
                        if(entityName==null){
                            continue innerJ;
                        }
                        inner:for (Field field : fields) {
                            field.setAccessible(true);
                            // 假如是分组的，设置分组字段
                            if(group){
                                if(field.getName().equals("groupCode")){
                                    field.set(entity,group_code);
                                }else if( field.getName().equals("subId")){
                                    if(clazz==BaseMaterial.class){
                                        field.set(entity,sub_id);
                                    }else{
                                        field.set(entity,Integer.valueOf(sub_id));
                                    }
                                }
                            }

                            if(field.getName().equals(entityName)){
                                // 假如是group的，ID 则要过滤00的内容
                                if(group){
                                    String[] split = content.split("\\.");
                                    StringBuilder str = new StringBuilder();
                                    for (int i = 0; i < split.length; i++) {
                                        if(i == split.length -1){


                                            char[] chars = split[i].toCharArray();
                                            boolean needComapre = true;
                                            for (int x = 0; x < chars.length; x++) {
                                                if(chars[x]=='0' && needComapre){
                                                    continue ;
                                                }
                                                needComapre = false;
                                                str.append(chars[x]);
                                            }
                                            str.append(".");
                                        }else{
                                            str.append(split[i]).append(".");
                                        }
                                    }
                                    field.set(entity,str.deleteCharAt(str.length() -1 ).toString());
                                }else{
                                    /*if (DateUtil.isCellDateFormatted(cell)) {
                                        Date dt = DateUtil.getJavaDate(cell.getNumericCellValue());
                                        String str = sdf.format(dt);
                                        Date date = sdf.parse(str);
                                        field.set(entity,date);*/
//                                    }else{
                                    String name = field.getName();
                                    if(outZero.contains(name)){
                                        int index = content.lastIndexOf('.');
                                        String content_pre = content.substring(0, index);
                                        String subStr = content.substring(index + 1);
                                        subStr = outFrontZero(subStr);
                                        content = content_pre +"."+ subStr;
                                    }
                                    field.set(entity, covertAttrType(field,content));

//                                    }
                                }

                            }

                        }
                    }
                }
                if(rowNum == 0){
                    continue;
                }
                list.add(entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static String outFrontZero(String subStr) {
        String sub_id = "";
        char[] chars = subStr.toCharArray();
        boolean needComapre = true;
        for (int i = 0; i < chars.length; i++) {
            if(chars[i]=='0' && needComapre){
                continue ;
            }
            needComapre = false;
            sub_id+=chars[i];
        }
        return sub_id;
    }

    /**
     * 类型转换 将cell 单元格格式转为 字段类型
     */
    private static Object covertAttrType(Field field, String oldVal) throws Exception {

        String fieldType = field.getType().getSimpleName();
        if ("String".equals(fieldType)) {
            return oldVal;
        }else if ("LocalDate".equals(fieldType)) {
            String[] split = oldVal.split("/");
            if(split[1].length() == 1){
                split[1] = "0"+split[1];
            }
            if(split[2].length() == 1){
                split[2] = "0"+split[2];
            }
            StringBuilder changeVal = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                changeVal.append( split[i]).append( "/") ;
            }
            changeVal.deleteCharAt(changeVal.length() - 1);
            oldVal = changeVal.toString();
            return LocalDate.parse(oldVal, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        }else if ("int".equals(fieldType) || "Integer".equals(fieldType)) {
            return Integer.parseInt(oldVal);
        }else if ("double".equals(fieldType) || "Double".equals(fieldType)) {
            return Double.parseDouble(oldVal);
        }else{
            return null;
        }
    }

    public static Map<String, String> getChangeMap() throws Exception{
        HashMap<String, String> hashMap = new HashMap<>();
        Connection con = getConn();
        Statement stmt = con.createStatement();
        String query = "select id,name from base_supplier ";
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()){
            //Retrieve by column name
            String id = rs.getString("id");
            String name = rs.getString("name");
            hashMap.put(name,id);
        }
        rs.close();
        return hashMap;
    }


    /**
     * 格式转为String
     * @param cell
     * @return
     */
    public static String getValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getRichStringCellValue().getString().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date dt = DateUtil.getJavaDate(cell.getNumericCellValue());
                    return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dt);
                } else {
                    // 防止数值变成科学计数法
                    String strCell = "";
                    Double num = cell.getNumericCellValue();
                    BigDecimal bd = new BigDecimal(num.toString());
                    if (bd != null) {
                        strCell = bd.toPlainString();
                    }
                    // 去除 浮点型 自动加的 .0
                    if (strCell.endsWith(".0")) {
                        strCell = strCell.substring(0, strCell.indexOf("."));
                    }
                    return strCell;
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
