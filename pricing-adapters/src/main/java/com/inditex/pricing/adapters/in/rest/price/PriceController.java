package com.inditex.pricing.adapters.in.rest.price;

import com.inditex.pricing.adapters.in.rest.price.dto.PriceResponseDto;
import com.inditex.pricing.application.ports.in.GetApplicablePriceQuery;
import com.inditex.pricing.application.ports.in.GetApplicablePriceUseCase;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RestController
@RequestMapping("/api/prices")
@Validated
public class PriceController {

    private final GetApplicablePriceUseCase useCase;
    private final PriceRestMapper mapper = new PriceRestMapper();

    public PriceController(GetApplicablePriceUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<PriceResponseDto> getApplicablePrice(
            @RequestParam("applicationDate") @NotNull @DateTimeFormat(iso = DATE_TIME) LocalDateTime applicationDate,
            @RequestParam("productId") @Min(1) long productId,
            @RequestParam("brandId") @Min(1) long brandId
    ) {
        var query = GetApplicablePriceQuery.of(brandId, productId, applicationDate);

        return useCase.execute(query)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}