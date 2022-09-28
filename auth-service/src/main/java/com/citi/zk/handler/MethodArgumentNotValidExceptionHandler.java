package com.citi.zk.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/8 14:49
 * @Desc:
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class MethodArgumentNotValidExceptionHandler {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public String exceptionHandler(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        String message = "";
        if (result.hasErrors()) {
            List<ObjectError> errors = result.getAllErrors();
            if (errors != null) {
                errors.forEach(p -> {
                    FieldError fieldError = (FieldError) p;
                    log.error("Data check failure : object{" + fieldError.getObjectName() + "},field{" + fieldError.getField() +
                            "},errorMessage{" + fieldError.getDefaultMessage() + "}");

                });
                if (errors.size() > 0) {
                    FieldError fieldError = (FieldError) errors.get(0);
                    message = fieldError.getDefaultMessage();
                }
            }
        }
        message = "".equals(message) ? "The parameter is incorrect！":message;
        return message;
    }
}


