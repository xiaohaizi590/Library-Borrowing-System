package net.togogo.controller;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import net.togogo.common.BusinessException;
import net.togogo.common.ResultCode;

@RestControllerAdvice
public class GlobalExceptionHandler {
   @ExceptionHandler(Exception.class)
    public ResponseEntity<BusinessException> handleException(Exception ex) {
        BusinessException errorResponse = BusinessException.builder()
                .resultCode(ResultCode.INTERNAL_SERVER_ERROR)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
}

