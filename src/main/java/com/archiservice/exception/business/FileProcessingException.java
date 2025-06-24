package com.archiservice.exception.business;

import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;

public class FileProcessingException extends BusinessException {
  public FileProcessingException() {
    super(ErrorCode.FILE_PROCESSING_ERROR);
  }

  public FileProcessingException(String message) {
    super(ErrorCode.FILE_PROCESSING_ERROR, message);
  }
}