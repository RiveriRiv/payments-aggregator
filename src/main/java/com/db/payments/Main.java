package com.db.payments;

import com.db.payments.dto.DailyReport;
import com.db.payments.dto.Payment;
import com.db.payments.parser.ExchangeRatesParser;
import com.db.payments.parser.PaymentsParser;
import com.db.payments.parser.RateConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class Main {

    private static final Map<LocalDate, DailyReport> reports = new HashMap<>();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar payments.jar <payments_file> <rates_file>");
            return;
        }

        String paymentsFilePath = args[0];
        String ratesFilePath = args[1];

        System.out.println("Processing files:");
        System.out.println("Payments: " + paymentsFilePath);
        System.out.println("Rates: " + ratesFilePath);

        try {
            processFiles(paymentsFilePath, ratesFilePath);
        } catch (Exception e) {
            System.out.println("An exception occurred: " + e.getMessage());
        }

        reports.clear();
    }

    private static void processFiles(String paymentsFile, String ratesFile) throws Exception {
        List<Payment> payments = PaymentsParser.readPayments(paymentsFile);
        Map<LocalDate, Map<String, BigDecimal>> exchangeRates = ExchangeRatesParser.readExchangeRates(ratesFile);

        calculateAndPrintResults(payments, exchangeRates);
    }

    private static void calculateAndPrintResults(List<Payment> payments, Map<LocalDate, Map<String, BigDecimal>> exchangeRates) {
        payments.forEach(payment -> processPayment(payment, exchangeRates));

        reports.values().forEach(DailyReport::printFullReport);
    }

    private static void processPayment(Payment payment, Map<LocalDate, Map<String, BigDecimal>> exchangeRates) {
        LocalDate date = payment.getDateTime().toLocalDate();
        reports.putIfAbsent(date, DailyReport.builder().date(date).build());

        DailyReport report = reports.get(date);
        Map<String, BigDecimal> ratesForDate = exchangeRates.getOrDefault(date, Collections.emptyMap());

        Optional<BigDecimal> amountInEUR = RateConverter.convertToEUR(payment, ratesForDate);
        report.addPayment(amountInEUR, payment);
    }
}