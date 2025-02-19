package com.db.payments.parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import static com.db.payments.util.PaymentsConsts.DATE_TIME_FORMATTER;
import static com.db.payments.util.PaymentsConsts.EUR;

public class ExchangeRatesParser {

    public static Map<LocalDate, Map<String, BigDecimal>> readExchangeRates(String filePath) throws IOException {
        Map<LocalDate, Map<String, BigDecimal>> exchangeRates = new HashMap<>();
        Files.readAllLines(Paths.get(filePath))
                .forEach(line -> processExchangeRatesLine(line, exchangeRates));

        return exchangeRates;
    }

    private static void processExchangeRatesLine(String line,
                                                 Map<LocalDate, Map<String, BigDecimal>> exchangeRates) throws IllegalArgumentException{
        String[] parts = line.split(";");

        if (parts.length != 4) {
            throw new IllegalArgumentException("Rates file contains wrong line: " + line);
        }

        LocalDateTime dateTime;
        LocalDate date;
        String fromCurrency;
        String toCurrency;
        BigDecimal rate;

        try {
            dateTime = LocalDateTime.parse(parts[0], DATE_TIME_FORMATTER);
            date = dateTime.toLocalDate();
            fromCurrency = parts[1];
            toCurrency = parts[2];
            rate = new BigDecimal(parts[3]);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Wrong date format in exchange rates file: " + parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong number format in exchange rates file: " + parts[3]);
        }

        if (EUR.equals(toCurrency)) {
            exchangeRates.computeIfAbsent(date, k -> new HashMap<>()).put(fromCurrency, rate);
        }
    }
}
