package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID recordId;

    private String ec;

    @JsonProperty("mid")
    private UUID id;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("document_number")
    private String documentNumber;

    private String name;

    @JsonProperty("created_at")
    private String createdAt;

    private boolean blocked;

    @JsonProperty("pix_enabled")
    private boolean pixEnabled;

    @JsonProperty("antifraud_enabled")
    private boolean antifraudEnabled;

    @JsonProperty("tokenization_enabled")
    private boolean tokenizationEnabled;

    @JsonProperty("velocity_enabled")
    private boolean velocityEnabled;

    @JsonProperty("smart_recurrency_enabled")
    private boolean smartRecurrencyEnabled;

    @JsonProperty("zero_dollar_auth_enabled")
    private boolean zeroDollarAuthEnabled;

    @JsonProperty("bin_query_enabled")
    private boolean binQueryEnabled;

    @JsonProperty("selective_auth_enabled")
    private boolean selectiveAuthEnabled;

    @JsonProperty("try_automatic_cancellation_enabled")
    private boolean tryAutomaticCancellationEnabled;

    @JsonProperty("force_braspag_auth_enabled")
    private boolean forceBraspagAuthEnabled;

    @JsonProperty("mtls_enabled")
    private boolean mtlsEnabled;

    @JsonProperty("webhook_enabled")
    private boolean webhookEnabled;

    @JsonProperty("white_list_ip_count")
    private int whiteListIpCount;

    @CreationTimestamp
    private LocalDateTime recordTimestamp;

    public UUID getRecordId() {
        return recordId;
    }

    public void setRecordId(UUID recordId) {
        this.recordId = recordId;
    }

    public String getEc() {
        return ec;
    }

    public void setEc(String ec) {
        this.ec = ec;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isPixEnabled() {
        return pixEnabled;
    }

    public void setPixEnabled(boolean pixEnabled) {
        this.pixEnabled = pixEnabled;
    }

    public boolean isAntifraudEnabled() {
        return antifraudEnabled;
    }

    public void setAntifraudEnabled(boolean antifraudEnabled) {
        this.antifraudEnabled = antifraudEnabled;
    }

    public boolean isTokenizationEnabled() {
        return tokenizationEnabled;
    }

    public void setTokenizationEnabled(boolean tokenizationEnabled) {
        this.tokenizationEnabled = tokenizationEnabled;
    }

    public boolean isVelocityEnabled() {
        return velocityEnabled;
    }

    public void setVelocityEnabled(boolean velocityEnabled) {
        this.velocityEnabled = velocityEnabled;
    }

    public boolean isSmartRecurrencyEnabled() {
        return smartRecurrencyEnabled;
    }

    public void setSmartRecurrencyEnabled(boolean smartRecurrencyEnabled) {
        this.smartRecurrencyEnabled = smartRecurrencyEnabled;
    }

    public boolean isZeroDollarAuthEnabled() {
        return zeroDollarAuthEnabled;
    }

    public void setZeroDollarAuthEnabled(boolean zeroDollarAuthEnabled) {
        this.zeroDollarAuthEnabled = zeroDollarAuthEnabled;
    }

    public boolean isBinQueryEnabled() {
        return binQueryEnabled;
    }

    public void setBinQueryEnabled(boolean binQueryEnabled) {
        this.binQueryEnabled = binQueryEnabled;
    }

    public boolean isSelectiveAuthEnabled() {
        return selectiveAuthEnabled;
    }

    public void setSelectiveAuthEnabled(boolean selectiveAuthEnabled) {
        this.selectiveAuthEnabled = selectiveAuthEnabled;
    }

    public boolean isTryAutomaticCancellationEnabled() {
        return tryAutomaticCancellationEnabled;
    }

    public void setTryAutomaticCancellationEnabled(boolean tryAutomaticCancellationEnabled) {
        this.tryAutomaticCancellationEnabled = tryAutomaticCancellationEnabled;
    }

    public boolean isForceBraspagAuthEnabled() {
        return forceBraspagAuthEnabled;
    }

    public void setForceBraspagAuthEnabled(boolean forceBraspagAuthEnabled) {
        this.forceBraspagAuthEnabled = forceBraspagAuthEnabled;
    }

    public boolean isMtlsEnabled() {
        return mtlsEnabled;
    }

    public void setMtlsEnabled(boolean mtlsEnabled) {
        this.mtlsEnabled = mtlsEnabled;
    }

    public boolean isWebhookEnabled() {
        return webhookEnabled;
    }

    public void setWebhookEnabled(boolean webhookEnabled) {
        this.webhookEnabled = webhookEnabled;
    }

    public int getWhiteListIpCount() {
        return whiteListIpCount;
    }

    public void setWhiteListIpCount(int whiteListIpCount) {
        this.whiteListIpCount = whiteListIpCount;
    }

    public LocalDateTime getRecordTimestamp() {
        return recordTimestamp;
    }

    public void setRecordTimestamp(LocalDateTime recordTimestamp) {
        this.recordTimestamp = recordTimestamp;
    }
}
