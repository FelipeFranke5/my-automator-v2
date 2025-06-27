package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class Merchant {

    @Id
    private UUID id;

    private String establishmentCode;
    private UUID merchantId;
    private String documentType;
    private String documentNumber;
    private String name;
    private boolean blocked;
    private boolean pixEnabled;
    private boolean antifraudEnabled;
    private boolean tokenizationEnabled;
    private boolean velocityEnabled;
    private boolean smartRecurrencyEnabled;
    private boolean zeroAuthEnabled;
    private boolean binQueryEnabled;
    private boolean selectiveAuthEnabled;
    private boolean automaticCancelationEnabled;
    private boolean forceBraspagAuthEnabled;
    private boolean mtlsEnabled;
    private boolean webhookEnabled;
    private byte whiteListIpCount;

    @CreationTimestamp
    private LocalDateTime recordTimestamp;

    public Merchant() {}

    public Merchant(
            UUID id,
            String establishmentCode,
            UUID merchantId,
            String documentType,
            String documentNumber,
            String name,
            boolean blocked,
            boolean pixEnabled,
            boolean antifraudEnabled,
            boolean tokenizationEnabled,
            boolean velocityEnabled,
            boolean smartRecurrencyEnabled,
            boolean zeroAuthEnabled,
            boolean binQueryEnabled,
            boolean selectiveAuthEnabled,
            boolean automaticCancelationEnabled,
            boolean forceBraspagAuthEnabled,
            boolean mtlsEnabled,
            boolean webhookEnabled,
            byte whiteListIpCount,
            LocalDateTime recordTimestamp) {
        this.id = id;
        this.establishmentCode = establishmentCode;
        this.merchantId = merchantId;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.name = name;
        this.blocked = blocked;
        this.pixEnabled = pixEnabled;
        this.antifraudEnabled = antifraudEnabled;
        this.tokenizationEnabled = tokenizationEnabled;
        this.velocityEnabled = velocityEnabled;
        this.smartRecurrencyEnabled = smartRecurrencyEnabled;
        this.zeroAuthEnabled = zeroAuthEnabled;
        this.binQueryEnabled = binQueryEnabled;
        this.selectiveAuthEnabled = selectiveAuthEnabled;
        this.automaticCancelationEnabled = automaticCancelationEnabled;
        this.forceBraspagAuthEnabled = forceBraspagAuthEnabled;
        this.mtlsEnabled = mtlsEnabled;
        this.webhookEnabled = webhookEnabled;
        this.whiteListIpCount = whiteListIpCount;
        this.recordTimestamp = recordTimestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEstablishmentCode() {
        return establishmentCode;
    }

    public void setEstablishmentCode(String establishmentCode) {
        this.establishmentCode = establishmentCode;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
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

    public boolean isZeroAuthEnabled() {
        return zeroAuthEnabled;
    }

    public void setZeroAuthEnabled(boolean zeroAuthEnabled) {
        this.zeroAuthEnabled = zeroAuthEnabled;
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

    public boolean isAutomaticCancelationEnabled() {
        return automaticCancelationEnabled;
    }

    public void setAutomaticCancelationEnabled(boolean automaticCancelationEnabled) {
        this.automaticCancelationEnabled = automaticCancelationEnabled;
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

    public byte getWhiteListIpCount() {
        return whiteListIpCount;
    }

    public void setWhiteListIpCount(byte whiteListIpCount) {
        this.whiteListIpCount = whiteListIpCount;
    }

    public LocalDateTime getRecordTimestamp() {
        return recordTimestamp;
    }

    public void setRecordTimestamp(LocalDateTime recordTimestamp) {
        this.recordTimestamp = recordTimestamp;
    }
}
