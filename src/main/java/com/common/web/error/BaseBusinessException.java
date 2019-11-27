package com.common.web.error;

/**
 * 业务异常
 *
 * @version V1.0
 **/
public class BaseBusinessException extends RuntimeException {
    private String errorCode;
    private String errorMessage;
    private String details;
    private Object[] params;

    private BaseBusinessException() {}

    public BaseBusinessException(String errorCode, Object...params) {
        this.errorCode = errorCode;
        this.params = params;
    }

    public BaseBusinessException errorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public BaseBusinessException errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public BaseBusinessException details(String details) {
        this.details = details;
        return this;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return this.details;
    }

    public BaseBusinessException params(Object[] params) {
        this.params = params;
        return this;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Object[] getParams() {
        return this.params;
    }
}
