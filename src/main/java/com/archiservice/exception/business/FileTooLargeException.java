package com.archiservice.exception.business;

import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;

public class FileTooLargeException extends BusinessException {
  public FileTooLargeException() {
    super(ErrorCode.FILE_TOO_LARGE);
  }

  public FileTooLargeException(String message) {
    super(ErrorCode.FILE_TOO_LARGE, message);
  }
}