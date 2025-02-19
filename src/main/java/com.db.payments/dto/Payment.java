package com.db.payments.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class Payment {
    public LocalDateTime dateTime;
    public String company;
    public String currency;
    public BigDecimal amount;
}
