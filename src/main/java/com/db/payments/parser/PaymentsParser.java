package com.db.payments.parser;

import com.db.payments.dto.Payment;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.db.payments.util.PaymentsConsts.DATE_TIME_FORMATTER;

public class PaymentsParser {

    public static List<Payment> readPayments(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath))
                .stream().map(PaymentsParser::processPaymentLine)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Payment processPaymentLine(String line) throws IllegalArgumentException {
        String[] parts = line.split(";");

        if (parts.length != 4) {
            throw new IllegalArgumentException("payments file contains wrong line: " + line);
        }

        LocalDateTime dateTime;
        String company;
        String currency;
        BigDecimal amount;

        try {
            dateTime = LocalDateTime.parse(parts[0], DATE_TIME_FORMATTER);
            company = parts[1];
            currency = parts[2];
            amount = new BigDecimal(parts[3]);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Wrong date format in payments file: " + parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong number format in payments file: " + parts[3]);
        }

        return Payment.builder()
                .dateTime(dateTime)
                .company(company)
                .currency(currency)
                .amount(amount)
                .build();
    }

}
