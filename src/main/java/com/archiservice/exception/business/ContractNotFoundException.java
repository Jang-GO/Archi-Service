package com.archiservice.exception.business;

import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;

public class ContractNotFoundException extends BusinessException {
  public ContractNotFoundException() {
    super(ErrorCode.CONTRACT_NOT_FOUND);
  }

  public ContractNotFoundException(String message) {
    super(ErrorCode.CONTRACT_NOT_FOUND, message);
  }
}