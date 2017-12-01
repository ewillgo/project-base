package cc.sportsdb.common.dto;

import org.springframework.data.annotation.Transient;

import java.util.LinkedHashMap;

public class Result<T> extends LinkedHashMap<String, Object> {

    @Transient private static final String STATUS = "status";
    @Transient private static final String MESSAGE = "message";
    @Transient private static final String DATA = "data";

    public Integer getStatus() {
        return (Integer) get(STATUS);
    }

    public void setStatus(Integer status) {
        put(STATUS, status);
    }

    public String getMessage() {
        return (String) get(MESSAGE);
    }

    public void setMessage(String message) {
        put(MESSAGE, message);
    }

    @SuppressWarnings("unchecked")
    public T getData() {
        return (T) get(DATA);
    }

    public void setData(T data) {
        put(DATA, data);
    }

    @Transient public static final Integer SUCCESS = 0;
    @Transient public static final Integer FAIL = -1;
    @Transient public static final String SUCCESS_MESSAGE = "Operation success.";
    @Transient public static final String FAIL_MESSAGE = "Operation fail.";
}
