package com.archiservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 내부 오류가 발생했습니다"),

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U404", "사용자를 찾을 수 없습니다."),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다"),

  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다"),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 토큰입니다"),
  LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED", "로그인이 필요합니다"),

  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),
  REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다."),
  CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTRACT_NOT_FOUND", "오늘 만료되는 계약이 없습니다."),
  NOT_MY_REVIEW(HttpStatus.FORBIDDEN, "NOT_MY_REVIEW", "본인의 리뷰만 수정 또는 삭제할 수 있습니다."),
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "올바르지 않은 입력값입니다."),

  ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "ALREADY_REVIEWED", "이미 해당 요금제에 대한 리뷰를 작성하셨습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;


  ErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}