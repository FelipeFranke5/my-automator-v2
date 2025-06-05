package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record CheckoutEnable3dsResponseBody(
        @JsonProperty("data") LocalDateTime timestamp, @JsonProperty("mensagem") String message) {}
