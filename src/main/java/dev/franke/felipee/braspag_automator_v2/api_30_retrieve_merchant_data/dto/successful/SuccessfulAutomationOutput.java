package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.successful;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record SuccessfulAutomationOutput(
        UUID id,
        @JsonProperty("numeroEc") String ecNumber,
        @JsonProperty("nome") String name,
        @JsonProperty("bloqueado") boolean blocked,
        @JsonProperty("dataRegistro") LocalDateTime recordTimestamp) {}
