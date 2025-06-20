package com.archiservice.advertisement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BannerClickRequest {
  private Long vasId;
  private String vasName;
}
