package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompletedAutomationOutputForExcel(
    UUID recordId,
    String ec,
    String alias,
    String name,
    boolean blocked,
    boolean testModeEnabled,
    boolean internationalPaymentEnabled,
    String notificationUrl,
    String returnUrl,
    String statusChangeUrl,
    boolean threeDSEnabled,
    String amexMid,
    boolean facialAuthEnabled,
    LocalDateTime recordTimestamp) {}
