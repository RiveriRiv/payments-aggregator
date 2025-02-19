package com.db.payments;

import com.db.payments.dto.DailyReport;
import com.db.payments.dto.Payment;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String EUR = "EUR";
    private static final Map<LocalDate, DailyReport> reports = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java -jar payments.jar <payments_file> <rates_file>");
            return;
        }

        String paymentsFilePath = args[0];
        String ratesFilePath = args[1];

        System.out.println("Processing files:");
        System.out.println("Payments: " + paymentsFilePath);
        System.out.println("Rates: " + ratesFilePath);

        processFiles(paymentsFilePath, ratesFilePath);
        reports.clear();
    }

    private static void processFiles(String paymentsFile, String ratesFile) throws IOException {
        List<Payment> payments = readPayments(paymentsFile);
        Map<LocalDate, Map<String, BigDecimal>> exchangeRates = readExchangeRates(ratesFile);

        calculateAndPrintResults(payments, exchangeRates);
    }

    private static List<Payment> readPayments(String filePath) throws IOException {
        List<Payment> payments = new ArrayList<>();

        Files.readAllLines(Paths.get(filePath))
                .forEach(line -> payments.add(processPaymentLine(line)));


        return payments;
    }

    private static Payment processPaymentLine(String line) {
        String[] parts = line.split(";");
        LocalDateTime dateTime = LocalDateTime.parse(parts[0], DATE_TIME_FORMATTER);
        String company = parts[1];
        String currency = parts[2];
        BigDecimal amount = new BigDecimal(parts[3]);

        return Payment.builder()
                .dateTime(dateTime)
                .company(company)
                .currency(currency)
                .amount(amount)
                .build();
    }

    private static Map<LocalDate, Map<String, BigDecimal>> readExchangeRates(String filePath) throws IOException {
        Map<LocalDate, Map<String, BigDecimal>> exchangeRates = new HashMap<>();
        Files.readAllLines(Paths.get(filePath))
                .forEach(line -> processExchangeRatesLine(line, exchangeRates));

        return exchangeRates;
    }

    private static void processExchangeRatesLine(String line, Map<LocalDate, Map<String, BigDecimal>> exchangeRates) {
        String[] parts = line.split(";");
        LocalDateTime dateTime = LocalDateTime.parse(parts[0], DATE_TIME_FORMATTER);
        LocalDate date = dateTime.toLocalDate();
        String fromCurrency = parts[1];
        String toCurrency = parts[2];
        BigDecimal rate = new BigDecimal(parts[3]);

        if (EUR.equals(toCurrency)) {
            exchangeRates.computeIfAbsent(date, k -> new HashMap<>()).put(fromCurrency, rate);
        }
    }

    private static void calculateAndPrintResults(List<Payment> payments, Map<LocalDate, Map<String, BigDecimal>> exchangeRates) {
        payments.forEach(payment -> processPayment(payment, exchangeRates));

        reports.values().forEach(DailyReport::printFullReport);
    }

    private static void processPayment(Payment payment, Map<LocalDate, Map<String, BigDecimal>> exchangeRates) {
        LocalDate date = payment.dateTime.toLocalDate();
        reports.putIfAbsent(date, DailyReport.builder().date(date).build());

        DailyReport report = reports.get(date);
        Map<String, BigDecimal> ratesForDate = exchangeRates.getOrDefault(date, Collections.emptyMap());

        Optional<BigDecimal> amountInEUR = convertToEUR(payment, ratesForDate);
        report.addPayment(amountInEUR, payment);
    }

    private static Optional<BigDecimal> convertToEUR(Payment payment, Map<String, BigDecimal> rates) {
        if (EUR.equals(payment.currency)) {
            return Optional.of(payment.amount);
        }
        BigDecimal rate = rates.get(payment.currency);
        return Optional.ofNullable(rate).map(r -> payment.amount.multiply(r));
    }
}