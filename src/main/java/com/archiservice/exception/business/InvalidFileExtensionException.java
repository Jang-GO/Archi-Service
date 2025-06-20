package com.archiservice.exception.business;

import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;

public class InvalidFileExtensionException extends BusinessException {
  public InvalidFileExtensionException() {
    super(ErrorCode.INVALID_FILE_EXTENSION);
  }

  public InvalidFileExtensionException(String message) {
    super(ErrorCode.INVALID_FILE_EXTENSION, message);
  }
}