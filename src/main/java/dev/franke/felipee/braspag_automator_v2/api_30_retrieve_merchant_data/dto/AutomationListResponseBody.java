package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.FailedScriptRecord;
import java.util.List;

public record AutomationListResponseBody(
        @JsonProperty("dadosAutomacoes") AutomationsRunningOutput runningAutomations,
        @JsonProperty("resultados") List<OutputMerchant> merchants,
        @JsonProperty("falhas") List<FailedScriptRecord> failedResults) {}
