package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record OutputMerchant(
    @JsonProperty("idRegistro") UUID recordId,
    @JsonProperty("numeroEc") String ec,
    @JsonProperty("merchantId") UUID merchantId,
    @JsonProperty("tipoDocumento") String documentType,
    @JsonProperty("numeroDocumento") String documentNumber,
    @JsonProperty("nomeFantasia") String name,
    @JsonProperty("dataCriacao") String createdAt,
    @JsonProperty("lojaBloqueada") boolean blocked,
    @JsonProperty("pix") boolean pixEnabled,
    @JsonProperty("antifraude") boolean antifraudEnabled,
    @JsonProperty("cartaoProtegido") boolean tokenizationEnabled,
    @JsonProperty("velocity") boolean velocityEnabled,
    @JsonProperty("recorrencia") boolean smartRecurrencyEnabled,
    @JsonProperty("zeroAuth") boolean zeroDollarAuthEnabled,
    @JsonProperty("consultaBin") boolean binQueryEnabled,
    @JsonProperty("autenticacaoSeletiva") boolean selectiveAuthEnabled,
    @JsonProperty("cancelamentoGarantido") boolean tryAutomaticCancellationEnabled,
    @JsonProperty("authBraspag") boolean forceBraspagAuthEnabled,
    @JsonProperty("mtls") boolean mtlsEnabled,
    @JsonProperty("webhook") boolean webhookEnabled,
    @JsonProperty("quantidadeIps") int whiteListIpCount,
    @JsonProperty("dataRegistro") LocalDateTime recordTimestamp) {}
