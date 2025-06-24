package com.archiservice.recommend.dto.response;

import com.archiservice.product.vas.dto.response.VasDetailResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendVasResponseDto {
    List<VasDetailResponseDto> vass;
}