package com.db.payments.parser;

import com.db.payments.dto.Payment;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static com.db.payments.util.PaymentsConsts.EUR;

public class RateConverter {

    public static Optional<BigDecimal> convertToEUR(Payment payment, Map<String, BigDecimal> rates) {
        if (EUR.equals(payment.getCurrency())) {
            return Optional.of(payment.getAmount());
        }
        BigDecimal rate = rates.get(payment.getCurrency());
        return Optional.ofNullable(rate).map(r -> payment.getAmount().multiply(r));
    }
}
