package cc.sportsdb.common.controller;

import cc.sportsdb.common.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionController.class);

    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public Result<Map<String, String>> validationException(BindException e, BindingResult bindingResult) {
        logger.error(e.getMessage(), e);
        Result<Map<String, String>> result = new Result<>();

        Map<String, String> errorMap = new LinkedHashMap<>();
        List<ObjectError> errorList = bindingResult.getAllErrors();
        for (ObjectError objectError : errorList) {
            DefaultMessageSourceResolvable argument = (DefaultMessageSourceResolvable) objectError.getArguments()[0];
            errorMap.put(argument.getCode(), objectError.getDefaultMessage());
        }

        result.setStatus(Result.FAIL);
        result.setMessage(Result.FAIL_MESSAGE);
        result.setData(errorMap);
        return result;
    }

}
