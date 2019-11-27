package com.common.util.excel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Excel列
 *
 * @version V1.0
 **/
public class ExcelColumn<T> {
    private String title;
    private String property;
    private ExcelColumnType type;

    /**
     * 数据处理
     */
    private BiConsumer<T, Cell> dataHandler;

    /**
     * 直接值，当值不为空时，将不从对象中读取值而直接使用该值
     */
    private Object directValue;
    private Long max;
    private Long min;
    private boolean nullable = false;

    /**
     * 做下拉的序列值
     * 为空时不做下拉限制，否则增加下拉限制
     */
    private List<String> selectedValues;

    public ExcelColumn() {
    }

    public ExcelColumn(String title, String property) {
        this.title = title;
        this.property = property;
        this.type = ExcelColumnType.TEXT;
        this.max = 255L;
    }

    public ExcelColumn(String title, String property, Long max) {
        this.title = title;
        this.property = property;
        this.max = max;
        this.type = ExcelColumnType.TEXT;
    }

    public static <T> ExcelColumn<T> of() {
        return new ExcelColumn<>();
    }

    public String getTitle() {
        return title;
    }

    public ExcelColumn<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    public ExcelColumnType getType() {
        return type;
    }

    public ExcelColumn<T> setType(ExcelColumnType type) {
        this.type = type;
        return this;
    }

    public Long getMax() {
        return max;
    }

    public ExcelColumn<T> setMax(Long max) {
        this.max = max;
        return this;
    }

    public Long getMin() {
        return min;
    }

    public ExcelColumn<T> setMin(Long min) {
        this.min = min;
        return this;
    }

    public String getProperty() {
        return property;
    }

    public ExcelColumn<T> setProperty(String property) {
        this.property = property;
        return this;
    }

    public boolean isNullable() {
        return nullable;
    }

    public ExcelColumn<T> setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public Object getDirectValue() {
        return directValue;
    }

    public ExcelColumn<T> setDirectValue(Object directValue) {
        this.directValue = directValue;
        return this;
    }

    public List<String> getSelectedValues() {
        return selectedValues;
    }

    public ExcelColumn<T> setSelectedValues(List<String> selectedValues) {
        this.selectedValues = selectedValues;
        return this;
    }

    /**
     * 如果有序列值，那么进行对应的处理
     * @param consumer
     * @return
     */
    ExcelColumn ifSelectedValues(Consumer<List<String>> consumer) {
        if (!CollectionUtils.isEmpty(this.selectedValues)) {
            consumer.accept(this.selectedValues);
        }
        return this;
    }

    ExcelColumn ifNoneSelectedValue(Runnable runnable) {
        if (CollectionUtils.isEmpty(this.selectedValues)) {
            runnable.run();
        }

        return this;
    }

    public BiConsumer<T, Cell> getDataHandler() {
        return dataHandler;
    }

    public ExcelColumn<T> setDataHandler(BiConsumer<T, Cell> dataHandler) {
        this.dataHandler = dataHandler;
        return this;
    }
}
