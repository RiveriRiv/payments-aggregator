package com.db.payments.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Builder
public class DailyReport {
    private final List<BigDecimal> paymentsEUR = new ArrayList<>();
    private final Map<String, BigDecimal> companyBalances = new HashMap<>();
    private final Map<String, BigDecimal> outstandingAmountsPerCurrency = new HashMap<>();
    private final List<String> notAvailableInEUR = new ArrayList<>();

    @Builder.Default
    private BigDecimal dailyVolume = BigDecimal.ZERO;
    private LocalDate date;

    public void addPayment(Optional<BigDecimal> amountInEUROptional, Payment payment) {
        if (amountInEUROptional.isPresent()) {
            BigDecimal amountInEUR = amountInEUROptional.get();
            paymentsEUR.add(amountInEUR);
            companyBalances.merge(payment.getCompany(), amountInEUR, BigDecimal::add);
            dailyVolume = dailyVolume.add(amountInEUR.abs());
        } else {
            notAvailableInEUR.add(payment.getCompany());
        }

        outstandingAmountsPerCurrency.merge(payment.getCurrency(), payment.getAmount(), BigDecimal::add);
    }

    public void printFullReport() {
        System.out.println("Date: " + date);
        System.out.println("Highest EUR value: " + getMaxValue(paymentsEUR).orElse(BigDecimal.ZERO));
        System.out.println("Lowest EUR value: " + getMinValue(paymentsEUR).orElse(BigDecimal.ZERO));

        if (isAllPaymentsInEUR()) {
            System.out.println("Transaction volume in EUR: " + dailyVolume);
        } else {
            System.out.println("Transaction volume in EUR: N.A.");
        }

        System.out.println("Outstanding amounts per company in EUR: " + getFormattedCompanyBalances());
        System.out.println("Outstanding amounts per currency: " + outstandingAmountsPerCurrency);
        System.out.println();
    }

    private Optional<BigDecimal> getMaxValue(List<BigDecimal> values) {
        return values.stream().max(BigDecimal::compareTo);
    }

    private Optional<BigDecimal> getMinValue(List<BigDecimal> values) {
        return values.stream().min(BigDecimal::compareTo);
    }

    private boolean isAllPaymentsInEUR() {
        return notAvailableInEUR.isEmpty();
    }

    private String getFormattedCompanyBalances() {
        return companyBalances.entrySet().stream()
                .map(entry -> formatBalanceForCompany(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private String formatBalanceForCompany(String company, BigDecimal balance) {
        if (notAvailableInEUR.contains(company)) {
            return company + "=N.A.";
        } else {
            return company + "=" + balance;
        }
    }
}
