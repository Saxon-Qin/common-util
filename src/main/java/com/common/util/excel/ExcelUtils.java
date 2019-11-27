package com.common.util.excel;

import com.common.util.ReflectionUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Excel辅助类
 *
 * @version V1.0
 **/
@SuppressWarnings({"WeakerAccess", "unused"})
public class ExcelUtils {
    private final static Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * 根据列信息创建包含标题信息及数据校验的Excel的WorkBook（xlsx格式）
     *
     * @param columnList 列信息清单
     * @return 创建的WorkBook
     */
    public static <T> XSSFWorkbook createXSSFEmptyWorkbook(List<ExcelColumn<T>> columnList) {
        return (XSSFWorkbook) Utils.createWorkbook(new XSSFWorkbook(), columnList);
    }

    /**
     * 根据列信息创建包含标题信息及数据校验的Excel的WorkBook（xls格式）
     *
     * @param columnList 列信息清单
     * @return 创建的WorkBook
     */
    public static <T> HSSFWorkbook createHSSFEmptyWorkbook(List<ExcelColumn<T>> columnList) {
        return (HSSFWorkbook) Utils.createWorkbook(new HSSFWorkbook(), columnList);
    }

    /**
     * 根据数据集生成2003格式的Excel数据表格
     *
     * @param dataList   数据集
     * @param columnList 需要生成的Excel列信息
     * @param <T>        数据集对象类型
     * @return 生成的Excel表格
     */
    public static <T> Workbook dataToHSSFWorkbook(
            List<T> dataList,
            List<ExcelColumn<T>> columnList) {
        return Utils.dataToWorkbook(new HSSFWorkbook(), dataList, columnList, null);
    }

    /**
     * 根据数据集生成2003格式的Excel数据表格
     * 第一行显示一个主标题
     *
     * @param dataList   数据集
     * @param columnList 需要生成的Excel列信息
     * @param <T>        数据集对象类型
     * @return 生成的Excel表格
     */
    public static <T> Workbook dataToHSSFWorkbook(
            List<T> dataList,
            List<ExcelColumn<T>> columnList,
            String mainTitle) {
        return Utils.dataToWorkbook(new HSSFWorkbook(), dataList, columnList, mainTitle);
    }

    /**
     * 根据数据集生成2007格式的Excel数据表格
     *
     * @param dataList   数据集
     * @param columnList 需要生成的Excel列信息
     * @param <T>        数据集对象类型
     * @return 生成的Excel表格
     */
    public static <T> Workbook dataToXSSFWorkbook(
            List<T> dataList,
            List<ExcelColumn<T>> columnList) {
        return Utils.dataToWorkbook(new XSSFWorkbook(), dataList, columnList, null);
    }

    /**
     * 根据数据集生成2007格式的Excel数据表格
     * 第一行显示一个主标题
     *
     * @param dataList   数据集
     * @param columnList 需要生成的Excel列信息
     * @param <T>        数据集对象类型
     * @return 生成的Excel表格
     */
    public static <T> Workbook dataToXSSFWorkbook(
            List<T> dataList,
            List<ExcelColumn<T>> columnList,
            String mainTitle) {
        return Utils.dataToWorkbook(new XSSFWorkbook(), dataList, columnList, mainTitle);
    }

    /**
     * 导出Excel到输出流中
     *
     * @param supplier Excel文件生成者
     * @param response 返回流
     */
    public static void writeExcelToResponse(
            Supplier<Workbook> supplier,
            HttpServletResponse response,
            String fileName) {
        Workbook workbook = supplier.get();

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        try {
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            logger.error("下载模板失败", e);
            throw ExcelException.of(ExcelErrorCodes.IO_ERROR).details("IO异常，请检查网络状况或重试");
        }
    }

    /**
     * 从上传的文件中读取内容
     *
     * @param uploadFile 上传的文件
     * @param clazz      读取的数据对象类型
     * @param <T>        读取的数据对象类型
     * @return 读取的数据
     */
    public static <T> List<T> read(
            String fileName,
            InputStream inputStream,
            List<ExcelColumn<T>> columns,
            Class<T> clazz) {
        List<T> dataList = new ArrayList<>(16);
        Utils.readExcelFileContent(fileName, inputStream, sheet -> {
            Iterator<Row> iterator = sheet.rowIterator();

            // 跳过标题行
            iterator.next();

            while (iterator.hasNext()) {
                dataList.add(Utils.rowToEntity(iterator.next(), clazz, columns));
            }
        });

        return dataList;
    }

    /**
     * 从Sheet中读取数据
     *
     * @param sheet   Excel表格
     * @param columns 表格列信息
     * @param clazz   读取的对象类型
     * @param <T>     读取的对象类型
     * @return 从Sheet中读取的数据
     */
    public static <T> List<T> read(
            Sheet sheet,
            List<ExcelColumn<T>> columns,
            Class<T> clazz) {
        List<T> dataList = new ArrayList<>(16);
        Iterator<Row> iterator = sheet.rowIterator();

        // 跳过标题行
        iterator.next();

        while (iterator.hasNext()) {
            dataList.add(Utils.rowToEntity(iterator.next(), clazz, columns));
        }

        return dataList;
    }

    /**
     * 设置字符串列长度限制
     * 支持xls及xlsx
     *
     * @param col          列
     * @param maxLength    最长长度
     * @param errorMessage 出错时的提示
     */
    public static void setLengthConstraint(
            Sheet sheet,
            int col,
            int maxLength,
            String errorMessage) {
        DataValidation validation;
        if (sheet instanceof HSSFSheet) {
            validation = Utils.createHSSFDataValidation(col, () -> DVConstraint.createNumericConstraint(DVConstraint.ValidationType.TEXT_LENGTH,
                    DVConstraint.OperatorType.BETWEEN, "1", String.valueOf(maxLength)));
        } else {
            validation = Utils.createXSSFDataValidation(sheet, col,
                    helper -> helper.createTextLengthConstraint(DataValidationConstraint.OperatorType.BETWEEN, "1", String.valueOf(maxLength)));
        }

        validation.createPromptBox("提示", errorMessage);
        sheet.addValidationData(validation);
    }

    /**
     * 设置列数字限制
     * 支持新老Excel版本
     *
     * @param col          列
     * @param errorMessage 出错的提示信息
     */
    public static void setNumericConstraint(
            Sheet sheet,
            int col,
            String errorMessage) {
        DataValidation validation;

        if (sheet instanceof HSSFSheet) {
            validation = Utils.createHSSFDataValidation(col, () -> DVConstraint.createNumericConstraint(DVConstraint.ValidationType.INTEGER,
                    DVConstraint.OperatorType.BETWEEN, "1", "65535"));
        } else {
            validation = Utils.createXSSFDataValidation(sheet, col,
                    helper -> helper.createNumericConstraint(DataValidationConstraint.ValidationType.DECIMAL,
                            DataValidationConstraint.OperatorType.BETWEEN, "1", "65535"));
        }

        validation.createPromptBox("提示", errorMessage);
        sheet.addValidationData(validation);
    }

    /**
     * 设置日期限制
     * 支持新老Excel版本
     *
     * @param col          列
     * @param errorMessage 出错时的提示信息
     */
    public static void setDateConstraint(
            Sheet sheet,
            int col,
            String errorMessage) {
        DataValidation validation;

        if (sheet instanceof HSSFSheet) {
            validation = Utils.createHSSFDataValidation(col, () -> DVConstraint.createDateConstraint(DVConstraint.OperatorType.BETWEEN, "2000/01/01",
                    "2999/01/01", "yyyy/MM/dd"));
        } else {
            validation = Utils.createXSSFDataValidation(sheet, col,
                    helper -> helper.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN, "2000/01/01", "2999/01/01", "yyyy/MM/dd"));
        }

        validation.createPromptBox("提示", errorMessage);
        sheet.addValidationData(validation);
    }

    private static class Utils {
        private static final int ROW_LIMIT = 10000;
        private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        /**
         * 获取需要添加限制的区域
         *
         * @param col 需要添加限制的列
         * @return 需要添加限制的区域
         */
        private static CellRangeAddressList getRegions(int col) {
            return new CellRangeAddressList(1, ROW_LIMIT, col, col);
        }

        /**
         * 构造HSSF类型的数据校验规则
         *
         * @param col      需要添加限制的列
         * @param supplier 限制条件生成函数
         * @return 生成的校验规则
         */
        private static DataValidation createHSSFDataValidation(
                int col,
                Supplier<DataValidationConstraint> supplier) {
            return new HSSFDataValidation(getRegions(col), supplier.get());
        }

        /**
         * 构造XSSF类型的数据校验规则
         *
         * @param sheet              Sheet页
         * @param col                需要添加限制的列
         * @param constraintFunction 限制条件生成函数
         * @return 生成的校验规则
         */
        private static DataValidation createXSSFDataValidation(
                Sheet sheet,
                int col,
                Function<XSSFDataValidationHelper, DataValidationConstraint> constraintFunction) {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            XSSFDataValidationHelper helper = new XSSFDataValidationHelper(xssfSheet);
            return helper.createValidation(constraintFunction.apply(helper), getRegions(col));
        }

        private static <T> void setLocalDateValue(
                Row row,
                T t,
                String title,
                Cell cell,
                String property) {
            Date date;

            try {
                date = cell.getDateCellValue();
            } catch (Exception ex) {
                logger.error("读取日期字段异常", ex);
                throw ExcelException.of(ExcelErrorCodes.READ_DATE_ERROR, "读取日期字段异常，行：" + row.getRowNum() + ", 列：" + title);
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            LocalDate localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
            try {
                ReflectionUtils.setField(t, property, localDate);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                logger.error("设置对象属性失败", e);
                throw ExcelException.of(ExcelErrorCodes.SET_OBJECT_FIELD_VALUE_ERROR)
                        .params(property)
                        .details("设置对象属性失败,属性：" + property);
            }
        }

        private static <T> void setDateValue(
                Row row,
                T t,
                String title,
                Cell cell,
                String property) {
            // 日期
            Date date;

            try {
                date = cell.getDateCellValue();
            } catch (Exception ex) {
                logger.error("读取日期字段异常", ex);
                throw ExcelException.of(ExcelErrorCodes.READ_DATE_ERROR)
                        .params(row.getRowNum(), title)
                        .details("读取日期字段异常，行：" + row.getRowNum() + ", 列：" + title);
            }

            try {
                ReflectionUtils.setField(t, property, date);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                logger.error("设置对象属性失败", e);
                throw ExcelException.of(ExcelErrorCodes.SET_OBJECT_FIELD_VALUE_ERROR)
                        .params(property)
                        .details("设置对象属性失败,属性：" + property);
            }
        }

        private static <T> void setStringValue(
                Row row,
                T t,
                int rowNum,
                ExcelColumn<T> column,
                String title,
                Cell cell,
                String property,
                Field field) {
            // 字符串
//            Length length = field.getAnnotation(Length.class);
//            String valueStr = cell.getStringCellValue();
//
//            if ("".equals(valueStr.trim())) {
//                if (column.isNullable()) {
//                    return;
//                }
//
//                logger.error("第{}行{}值不能为空", rowNum, title);
//
//                throw ExcelException.of(ExcelErrorCodes.VALUE_NULL).params(rowNum, title).details("第" + rowNum + "行" + title + "不能为空");
//            }
//
//            if (null != length) {
//                int maxLength = length.max();
//                if (valueStr.length() > maxLength) {
//                    logger.error("属性{}长度不能超过{}", property, maxLength);
//                    throw ExcelException.of(ExcelErrorCodes.VALUE_TOO_LONG).params(rowNum, title, maxLength)
//                            .details("第" + rowNum + "行" + title + "长度不能超过" + maxLength);
//                }
//            }

            String valueStr = cell.getStringCellValue();

            try {
                ReflectionUtils.setField(t, property, valueStr);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                logger.error("设置对象属性失败", e);
                throw ExcelException.of(ExcelErrorCodes.SET_OBJECT_FIELD_VALUE_ERROR)
                        .params(property)
                        .details("设置对象属性失败,属性：" + property);
            }
        }

        private static <T> void setNumberValue(
                Row row,
                T t,
                ExcelColumn<T> column,
                Cell cell,
                Class fieldClass) {
            Object obj;

            double numericValue;

            try {
                numericValue = cell.getNumericCellValue();
            } catch (Exception ex) {
                logger.error("读取数值异常", ex);
                throw ExcelException.of(ExcelErrorCodes.READ_NUMBER_ERROR).params(String.valueOf(row.getRowNum()), column.getTitle())
                        .details("读取数值异常，行：" + row.getRowNum() + ", 字段：" + column.getTitle());
            }

            if (null != column.getMax() && column.getMax() < numericValue) {
                throw ExcelException.of(ExcelErrorCodes.NUMBER_TOO_LARGE).params(
                        String.valueOf(row.getRowNum()), column.getTitle(),
                        column.getMax());
            }

            if (null != column.getMin() && column.getMin() > numericValue) {
                throw ExcelException.of(ExcelErrorCodes.NUMBER_TOO_SMALL).params(
                        String.valueOf(row.getRowNum()), column.getTitle(),
                        column.getMax());
            }

            String fieldClassName = fieldClass.getSimpleName();
            switch (fieldClassName) {
                case "int":
                case "Integer":
                    obj = (int) numericValue;
                    break;
                case "long":
                case "Long":
                    obj = (long) numericValue;
                    break;
                case "float":
                case "Float":
                    obj = (float) numericValue;
                    break;
                case "byte":
                case "Byte":
                    obj = (byte) numericValue;
                    break;
                case "char":
                case "Char":
                    obj = (char) numericValue;
                    break;
                case "short":
                case "Short":
                    obj = (short) numericValue;
                    break;
                default:
                    obj = numericValue;
                    break;
            }

            // 数字
            try {
                ReflectionUtils.setField(t, column.getProperty(), obj);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                logger.error("设置对象属性失败", e);
                throw ExcelException.of(ExcelErrorCodes.SET_OBJECT_FIELD_VALUE_ERROR)
                        .params(column.getProperty())
                        .details("设置对象属性失败,属性：" + column.getProperty());
            }
        }

        private static <T> void setBooleanValue(
                T t,
                Cell cell,
                String property) {
            // 如果是布尔类型
            String str = cell.getStringCellValue();
            boolean b = "是".equals(str) || "true".equals(str);

            try {
                ReflectionUtils.setField(t, property, b);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                logger.error("设置对象属性失败", e);
                throw ExcelException.of(ExcelErrorCodes.SET_OBJECT_FIELD_VALUE_ERROR)
                        .params(property)
                        .details("设置对象属性失败,属性：" + property);
            }
        }

        /**
         * 将Excel中的某一行转换成实体对象
         */
        private static <T> T rowToEntity(
                Row row,
                Class<T> clazz,
                List<ExcelColumn<T>> columns) throws ExcelException {
            T t;
            try {
                t = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("实例化对象失败", e);
                throw ExcelException.of(ExcelErrorCodes.INTERNAL_ERROR).details("实例化对象失败");
            }

            int rowNum = row.getRowNum();

            for (int i = 0; i < columns.size(); i++) {
                ExcelColumn<T> column = columns.get(i);
                String title = column.getTitle();
                Cell cell = row.getCell(i);
                if (null == cell) {
                    if (column.isNullable()) {
                        continue;
                    }

                    logger.error("第{}行{}值不能为空", rowNum, title);
                    throw ExcelException.of(ExcelErrorCodes.VALUE_NULL).params(rowNum, title).details("第" + rowNum + "行" + title + "不能为空");
                }

                if (null != column.getDataHandler()) {
                    column.getDataHandler().accept(t, cell);
                    continue;
                }

                String property = column.getProperty();
                Field field;
                try {
                    field = clazz.getDeclaredField(property);
                } catch (NoSuchFieldException e) {
                    logger.error("属性不存在！", e);
                    throw ExcelException.of(ExcelErrorCodes.FIELD_NOT_EXISTS, "内部异常，请联系管理员");
                }

                Class fieldClass = field.getType();
                if (Boolean.class.equals(fieldClass) || (fieldClass.getName().equals("boolean"))) {
                    Utils.setBooleanValue(t, cell, property);
                } else if (Number.class.isAssignableFrom(fieldClass) || fieldClass.isPrimitive()) {
                    Utils.setNumberValue(row, t, column, cell, fieldClass);
                } else if (fieldClass.equals(String.class)) {
                    Utils.setStringValue(row, t, rowNum, column, title, cell, property, field);
                } else if (fieldClass.equals(Date.class)) {
                    Utils.setDateValue(row, t, title, cell, property);
                } else if (fieldClass.equals(LocalDate.class)) {
                    Utils.setLocalDateValue(row, t, title, cell, property);
                }
            }

            return t;
        }

        /**
         * 将Excel中的某一行转换成实体对象
         */
        private static <T> T rowToEntity(
                Row row,
                Class<T> clazz,
                List<String> propertyList,
                List<String> titleList) {
            List<ExcelColumn<T>> columnList = new ArrayList<>(propertyList.size());
            for (int i = 0; i < propertyList.size(); i++) {
                String property = propertyList.get(i);
                String title = titleList.get(i);
                ExcelColumn<T> column = new ExcelColumn<>(title, property);
                columnList.add(column);
            }

            return rowToEntity(row, clazz, columnList);
        }

        private static <T> Workbook createWorkbook(
                Workbook workbook,
                List<ExcelColumn<T>> columnList) {
            Sheet sheet = workbook.createSheet();
            Row row = sheet.createRow(0);

            // 设置文本格式
            CellStyle textStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            textStyle.setDataFormat(format.getFormat("@"));

            for (int i = 0; i < columnList.size(); i++) {
                ExcelColumn<T> column = columnList.get(i);
                Cell cell = row.createCell(i);
                cell.setCellValue(column.getTitle());

                // 如果列有提供序列值，那么限制其值从下拉序列中选择
                int pi = i;
                column.ifSelectedValues(list -> {
                    CellRangeAddressList regions = new CellRangeAddressList(1, 65535, pi, pi);
                    DataValidation validation;
                    String[] values = new String[list.size()];
                    values = list.toArray(values);

                    if (sheet instanceof HSSFSheet) {
                        DVConstraint constraint = DVConstraint.createExplicitListConstraint(values);
                        validation = new HSSFDataValidation(regions, constraint);
                    } else {
                        XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
                        DataValidationConstraint dvConstraint = validationHelper.createExplicitListConstraint(values);
                        validation = validationHelper.createValidation(dvConstraint, regions);
                    }

                    sheet.addValidationData(validation);
                }).ifNoneSelectedValue(() -> {
                    switch (column.getType()) {
                        case TEXT:
                            sheet.setDefaultColumnStyle(pi, textStyle);
                            setLengthConstraint(sheet, pi, column.getMax().intValue(),
                                    column.getTitle() + "长度不能超过" + column.getMax().intValue() + "位");
                            break;
                        case NUMBER:
                            setNumericConstraint(sheet, pi, column.getTitle() + "必须输入数字");
                            break;
                        case DATE:
                            setDateConstraint(sheet, pi, column.getTitle() + "必须输入日期");
                            break;
                    }
                });
            }

            return workbook;
        }

        /**
         * 将数据转换成Excel的Workbook
         *
         * @param dataList 数据
         * @param clazz    数据实体对象
         * @param <T>      数据实体对象类型
         */
        private static <T> Workbook dataToWorkbook(
                Workbook workbook,
                List<T> dataList,
                List<ExcelColumn<T>> columnList,
                String mainTitle) {
            List<String> titleList = new ArrayList<>(16);
            List<String> propertyList = new ArrayList<>(16);

            // 存储直接值
            Map<String, Object> directValueMap = new HashMap<>(16);

            columnList.forEach(column -> {
                titleList.add(column.getTitle());
                propertyList.add(column.getProperty());
                if (null != column.getDirectValue()) {
                    directValueMap.put(column.getProperty(), column.getDirectValue());
                }
            });

            Sheet sheet = workbook.createSheet("Sheet1");

            // 如果传入的mainTitle不为空，那么需要展示一个主标题
            int startRow = 0;
            if (StringUtils.isNotBlank(mainTitle)) {
                Row mainTitleRow = sheet.createRow(0);

                Cell cell = mainTitleRow.createCell(0);
                cell.setCellValue(mainTitle);

                CellStyle mainTitleCellStyle = getMainTitleStyle(workbook);
                cell.setCellStyle(mainTitleCellStyle);

                for (int i = 1; i < columnList.size(); i++) {
                    mainTitleRow.createCell(i).setCellStyle(mainTitleCellStyle);
                }

                // 合并单元格
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnList.size() - 1));

                startRow = 1;
            }

            // 创建标题行
            Row titleRow = sheet.createRow(startRow);
            CellStyle titleCellStyle = getTitleCellStyle(workbook);
            for (int col = 0, size = titleList.size(); col < size; col++) {
                Cell cell = titleRow.createCell(col);
                cell.setCellStyle(titleCellStyle);
                cell.setCellValue(titleList.get(col));
            }

            if (CollectionUtils.isEmpty(dataList)) {
                return workbook;
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            // 创建数据行
            for (int rowIndex = 0, size = dataList.size(); rowIndex < size; rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1 + startRow);
                T t = dataList.get(rowIndex);

                for (int col = 0, pSize = propertyList.size(); col < pSize; col++) {
                    String property = propertyList.get(col);
                    Cell cell = row.createCell(col);
                    cell.setCellStyle(getContentStyle(workbook));

                    if (StringUtils.isBlank(property)) {
                        continue;
                    }

                    // 如果有直接值，那么不从对象中读取
                    Object obj = directValueMap.get(property);
                    if (null == obj) {
                        try {
                            obj = ReflectionUtils.getFieldValue(t, property);
                        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                            logger.error("读取对象属性值失败");
                            throw ExcelException.of(ExcelErrorCodes.GET_OBJECT_FIELD_VALUE_ERROR)
                                    .details("读取对象属性值失败，错误消息：" + e.getMessage());
                        }
                        if (null == obj) {
                            continue;
                        }
                    }

                    if (obj instanceof Date) {
                        Date date = (Date) obj;
                        cell.setCellValue(format.format(date));
                    } else if (obj instanceof LocalDate) {
                        cell.setCellValue(((LocalDate) obj).format(dateFormatter));
                    } else if (obj instanceof LocalDateTime) {
                        cell.setCellValue(((LocalDateTime) obj).format(timeFormatter));
                    } else {
                        cell.setCellValue(obj.toString());
                    }
                }
            }

            return workbook;
        }

        /**
         * 获取标题行的样式
         *
         * @return 样式
         */
        private static CellStyle getTitleCellStyle(Workbook workbook) {
            CellStyle cellStyle = workbook.createCellStyle();

            // 设置背景
            if (workbook instanceof XSSFWorkbook) {
                XSSFColor color = new XSSFColor(java.awt.Color.GRAY);
                ((XSSFCellStyle) cellStyle).setFillForegroundColor(color);
                cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
            } else {
                cellStyle.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
                cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            }

            Font font = workbook.createFont();
            font.setColor(HSSFColor.WHITE.index);
            cellStyle.setFont(font);
            cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            return setBorder(cellStyle);
        }

        /**
         * 获取内容样式
         *
         * @param workbook 表格
         * @return 单元格样式
         */
        private static CellStyle getContentStyle(Workbook workbook) {
            return setBorder(workbook.createCellStyle());
        }

        /**
         * 设置边框
         *
         * @param cellStyle 单元格样式
         * @return 单元格样式
         */
        private static CellStyle setBorder(CellStyle cellStyle) {
            cellStyle.setBorderBottom(BorderFormatting.BORDER_THIN);
            cellStyle.setBorderTop(BorderFormatting.BORDER_THIN);
            cellStyle.setBorderLeft(BorderFormatting.BORDER_THIN);
            cellStyle.setBorderRight(BorderFormatting.BORDER_THIN);
            cellStyle.setLeftBorderColor(HSSFColor.GREY_50_PERCENT.index);
            cellStyle.setRightBorderColor(HSSFColor.GREY_50_PERCENT.index);
            cellStyle.setTopBorderColor(HSSFColor.GREY_50_PERCENT.index);
            cellStyle.setBottomBorderColor(HSSFColor.GREY_50_PERCENT.index);
            return cellStyle;
        }

        /**
         * 获取主标题行样式
         *
         * @param workbook 表格
         * @return 样式
         */
        private static CellStyle getMainTitleStyle(Workbook workbook) {
            CellStyle cellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            cellStyle.setFont(font);
            cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            return setBorder(cellStyle);
        }

        /**
         * 上传Excel
         */
        private static void readExcelFileContent(
                String fileName,
                InputStream inputStream,
                Consumer<Sheet> consumer) {
            Sheet sheet;

            assert fileName != null;
            if (fileName.endsWith(".xlsx")) {
                try {
                    sheet = new XSSFWorkbook(inputStream).getSheetAt(0);
                } catch (IOException e) {
                    logger.error("读取上传的文件内容失败！", e);
                    throw ExcelException.of(ExcelErrorCodes.IO_ERROR).details("IO异常，请检查网络状况或重试");
                }
            } else if (fileName.endsWith(".xls")) {
                try {
                    sheet = new HSSFWorkbook(inputStream).getSheetAt(0);
                } catch (IOException e) {
                    logger.error("读取上传的文件内容失败！", e);
                    throw ExcelException.of(ExcelErrorCodes.IO_ERROR).details("IO异常，请检查网络状况或重试");
                }
            } else {
                logger.error("文件不是Excel格式，无法处理");
                throw ExcelException.of(ExcelErrorCodes.FILE_NOT_EXCEL, "文件格式不是正确的Excel格式");
            }

            consumer.accept(sheet);
        }
    }
}
