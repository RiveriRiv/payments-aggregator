package com.db.payments.util;

import java.time.format.DateTimeFormatter;

public final class PaymentsConsts {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String EUR = "EUR";

    private PaymentsConsts() {
    }
}
