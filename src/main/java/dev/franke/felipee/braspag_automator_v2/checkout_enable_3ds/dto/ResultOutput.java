package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultOutput(@JsonProperty("numeroEc") String ec, @JsonProperty("mensagemRetorno") String message) {}
