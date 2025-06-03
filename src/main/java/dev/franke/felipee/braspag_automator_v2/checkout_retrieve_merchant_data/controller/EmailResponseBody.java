package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record EmailResponseBody(
    @JsonProperty("dataHora") LocalDateTime timestamp, @JsonProperty("mensagem") String message) {}
