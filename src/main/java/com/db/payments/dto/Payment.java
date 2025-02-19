package com.db.payments.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class Payment {

    private LocalDateTime dateTime;
    private String company;
    private String currency;
    private BigDecimal amount;
}
