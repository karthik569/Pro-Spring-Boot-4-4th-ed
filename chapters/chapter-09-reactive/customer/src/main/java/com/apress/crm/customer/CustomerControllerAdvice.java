package com.apress.crm.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class CustomerControllerAdvice {

    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleBindException(WebExchangeBindException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setType(URI.create("https://api.crm.com/errors/validation"));
        problemDetail.setTitle("Validation Error");
        
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }
}
