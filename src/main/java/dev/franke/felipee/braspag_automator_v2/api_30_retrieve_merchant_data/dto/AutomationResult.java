package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AutomationResult(@JsonProperty("mensagemRetorno") String message) {}
