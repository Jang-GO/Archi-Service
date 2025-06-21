package com.archiservice.user.dto.request;

import com.archiservice.product.bundle.domain.ProductBundle;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CreateContractRequestDto {
    private ProductBundle bundle;
    private Long price;
}
