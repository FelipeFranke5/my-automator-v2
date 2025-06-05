package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record OutputMerchant(
        @JsonProperty("idRegistro") UUID recordId,
        @JsonProperty("numeroEc") String ec,
        @JsonProperty("dataRegistro") LocalDateTime recordTimestamp) {}
