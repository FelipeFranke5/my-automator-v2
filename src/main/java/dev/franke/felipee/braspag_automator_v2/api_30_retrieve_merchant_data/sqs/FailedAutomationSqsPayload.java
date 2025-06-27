package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.sqs;

public record FailedAutomationSqsPayload(String establishmentCode, Exception exception) {}
