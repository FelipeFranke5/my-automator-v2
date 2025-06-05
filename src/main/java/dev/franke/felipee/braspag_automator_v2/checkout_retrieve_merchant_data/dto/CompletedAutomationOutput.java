package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompletedAutomationOutput(
        @JsonProperty("numeroEc") String ec, @JsonProperty("lojaBloqueada") boolean blocked) {}
