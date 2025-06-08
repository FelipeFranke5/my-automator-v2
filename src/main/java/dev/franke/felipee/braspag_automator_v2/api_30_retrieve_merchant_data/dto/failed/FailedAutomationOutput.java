package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto.failed;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record FailedAutomationOutput(
        UUID id,
        @JsonProperty("numeroEc") String ecNumber,
        @JsonProperty("mensagem") String message,
        @JsonProperty("dataRegistro") LocalDateTime recordTimestamp) {}
