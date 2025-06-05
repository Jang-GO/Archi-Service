package com.archiservice.exception.handler;

import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
    if (e.getErrorCode().getStatus().is5xxServerError()) {
      log.error("Business Error: {}", e.getMessage(), e);
    }
    return ResponseEntity
        .status(e.getErrorCode().getStatus())
        .body(new ErrorResponse(
            e.getErrorCode().getCode(),
            e.getMessage(),
            request.getRequestURI()
        ));
  }

}