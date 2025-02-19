package com.db.payments;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;

class MainTest {
    private Path paymentsFile;
    private Path ratesFile;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() throws IOException {
        paymentsFile = Files.createTempFile("payments", ".csv");
        ratesFile = Files.createTempFile("rates", ".csv");

        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        Files.deleteIfExists(paymentsFile);
        Files.deleteIfExists(ratesFile);
    }

    @Test
    void testMainMethodSinglePaymentEUR() throws Exception {
        Files.writeString(paymentsFile, "2025-02-14 10:00:00;Company A;EUR;1000\n", StandardOpenOption.APPEND);
        Files.writeString(ratesFile, "2025-02-14 00:00:00;USD;EUR;0.9\n", StandardOpenOption.APPEND);

        String[] args = {paymentsFile.toString(), ratesFile.toString()};
        Main.main(args);

        String output = outputStream.toString();

        assertThat(output).contains("Date: 2025-02-14");
        assertThat(output).contains("Highest EUR value: 1000");
        assertThat(output).contains("Lowest EUR value: 1000");
        assertThat(output).contains("Transaction volume in EUR: 1000");
        assertThat(output).contains("Outstanding amounts per company in EUR: {Company A=1000}");
        assertThat(output).contains("Outstanding amounts per currency: {EUR=1000}");
    }

    @Test
    void testMultiplePaymentsEUR() throws Exception {
        Files.writeString(paymentsFile, """
                2025-02-14 10:00:00;Company A;EUR;-1000
                2025-02-14 11:00:00;Company A;EUR;1500
                2025-02-14 12:00:00;Company B;EUR;20
                """, StandardOpenOption.APPEND);

        String[] args = {paymentsFile.toString(), ratesFile.toString()};
        Main.main(args);

        String output = outputStream.toString();

        assertThat(output).contains("Highest EUR value: 1500");
        assertThat(output).contains("Lowest EUR value: -1000");
        assertThat(output).contains("Transaction volume in EUR: 2520");
        assertThat(output).contains("Outstanding amounts per company in EUR: {Company A=500, Company B=20}");
        assertThat(output).contains("Outstanding amounts per currency: {EUR=520}");
    }

    @Test
    void testPaymentWithCurrencyConversion() throws Exception {
        Files.writeString(paymentsFile, "2025-02-14 10:00:00;Company A;USD;1000\n", StandardOpenOption.APPEND);
        Files.writeString(ratesFile, "2025-02-14 00:00:00;USD;EUR;0.85\n", StandardOpenOption.APPEND);

        String[] args = {paymentsFile.toString(), ratesFile.toString()};
        Main.main(args);

        String output = outputStream.toString();

        assertThat(output).contains("Highest EUR value: 850.00");
        assertThat(output).contains("Lowest EUR value: 850.00");
        assertThat(output).contains("Transaction volume in EUR: 850.00");
        assertThat(output).contains("Outstanding amounts per company in EUR: {Company A=850.00}");
        assertThat(output).contains("Outstanding amounts per currency: {USD=1000}");
    }

    @Test
    void testPaymentWithoutExchangeRate() throws Exception {
        Files.writeString(paymentsFile, """
                2025-02-14 10:00:00;Company A;GBP;500
                2025-02-14 11:00:00;Company A;EUR;1500
                2025-02-14 12:00:00;Company B;EUR;200
                """, StandardOpenOption.APPEND);

        String[] args = {paymentsFile.toString(), ratesFile.toString()};
        Main.main(args);

        String output = outputStream.toString();

        assertThat(output).contains("Highest EUR value: 1500");
        assertThat(output).contains("Lowest EUR value: 200");
        assertThat(output).contains("Transaction volume in EUR: N.A.");
        assertThat(output).contains("Outstanding amounts per company in EUR: {Company A=N.A., Company B=200}");
        assertThat(output).contains("Outstanding amounts per currency: {EUR=1700, GBP=500}");
    }

    @Test
    void testNegativePaymentAmount() throws Exception {
        Files.writeString(paymentsFile, "2025-02-14 10:00:00;Company A;EUR;-500\n", StandardOpenOption.APPEND);

        String[] args = {paymentsFile.toString(), ratesFile.toString()};
        Main.main(args);

        String output = outputStream.toString();

        assertThat(output).contains("Highest EUR value: -500");
        assertThat(output).contains("Lowest EUR value: -500");
        assertThat(output).contains("Transaction volume in EUR: 500");
        assertThat(output).contains("Outstanding amounts per company in EUR: {Company A=-500}");
        assertThat(output).contains("Outstanding amounts per currency: {EUR=-500}");
    }

    @Test
    void testWrongPaymentLineFormat() throws Exception {
        Files.writeString(paymentsFile, "2025-02-14 10:00:00Company A;EUR;-500\n", StandardOpenOption.APPEND);

        String[] args = {paymentsFile.toString(), ratesFile.toString()};
        Main.main(args);

        String output = outputStream.toString();

        assertThat(output).contains("An exception occurred: ");
    }

    @Test
    void testWrongExchangeRateLineFormat() throws Exception {
        Files.writeString(paymentsFile, "2025-02-14 10:00:00;Company A;USD;1000\n", StandardOpenOption.APPEND);
        Files.writeString(ratesFile, "2025-02-14 00:00:00;USDEUR;0.85\n", StandardOpenOption.APPEND);

        String[] args = {paymentsFile.toString(), ratesFile.toString()};
        Main.main(args);

        String output = outputStream.toString();

        assertThat(output).contains("An exception occurred: ");
    }

    @Test
    void testWrongExchangeRateDateFormat() throws Exception {
        Files.writeString(paymentsFile, "2025-02-14 10:00:00;Company A;USD;1000\n", StandardOpenOption.APPEND);
        Files.writeString(ratesFile, "2025-02-14 00;USD;EUR;0.85\n", StandardOpenOption.APPEND);

        String[] args = {paymentsFile.toString(), ratesFile.toString()};
        Main.main(args);

        String output = outputStream.toString();

        assertThat(output).contains("An exception occurred: Wrong date format in exchange rates file:");
    }

    @Test
    void testWrongPaymentsDateFormat() throws Exception {
        Files.writeString(paymentsFile, "2025-02-14 10;Company A;USD;1000\n", StandardOpenOption.APPEND);
        Files.writeString(ratesFile, "2025-02-14 00:00:00;USD;EUR;0.85\n", StandardOpenOption.APPEND);

        String[] args = {paymentsFile.toString(), ratesFile.toString()};
        Main.main(args);

        String output = outputStream.toString();

        assertThat(output).contains("An exception occurred: Wrong date format in payments file:");
    }
}
