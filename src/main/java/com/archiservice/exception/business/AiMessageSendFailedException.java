package com.archiservice.exception.business;

import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;

public class AiMessageSendFailedException extends BusinessException {
  public AiMessageSendFailedException() {
    super(ErrorCode.AI_MESSAGE_SEND_FAILED);
  }

  public AiMessageSendFailedException(String customMessage) {
    super(ErrorCode.AI_MESSAGE_SEND_FAILED, customMessage);
  }

  public AiMessageSendFailedException(Throwable cause) {
    super(ErrorCode.AI_MESSAGE_SEND_FAILED, cause.getMessage());
  }
}