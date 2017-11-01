package cc.sportsdb.common.dto;

import java.io.Serializable;

public class Result<T> implements Serializable {
    private Integer status;
    private String message;
    private T data;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static final Integer SUCCESS = 0;
    public static final Integer FAIL = 10000;
    public static final String SUCCESS_MESSAGE = "Operation success.";
    public static final String FAIL_MESSAGE = "Operation fail.";
}
