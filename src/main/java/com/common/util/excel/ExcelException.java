package com.common.util.excel;

/**
 * 业务异常
 *
 * @version V1.0
 **/
public class ExcelException extends RuntimeException {
    private String code;
    private Object[] params;
    private String message;
    private String details;

    private ExcelException() {

    }

    public static ExcelException of(String code) {
        return new ExcelException().code(code);
    }

    public static ExcelException of(String code, String message) {
        return new ExcelException().code(code).message(message);
    }

    public ExcelException params(Object...params) {
        this.params = params;
        return this;
    }

    public ExcelException code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public ExcelException message(String message) {
        this.message = message;
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public ExcelException details(String details) {
        this.details = details;
        return this;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return this.details;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Object[] getParams() {
        return this.params;
    }
}
