package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AutomationsRunningOutput(
        @JsonProperty("automacoesSendoExecutadas") byte numberOfAutomations,
        @JsonProperty("mensagem") String message) {}
