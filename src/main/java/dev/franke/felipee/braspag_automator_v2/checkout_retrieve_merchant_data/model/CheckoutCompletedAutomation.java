package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class CheckoutCompletedAutomation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID recordId;

    @JsonProperty("ec")
    private String ec;

    @JsonProperty("mid")
    private UUID id;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("document_number")
    private String documentNumber;

    @JsonProperty("name")
    private String name;

    @JsonProperty("blocked")
    private boolean blocked;

    @JsonProperty("test_mode")
    private boolean testModeEnabled;

    @JsonProperty("accept_international_card")
    private boolean internationalPaymentEnabled;

    @JsonProperty("notification_url")
    private String notificationUrl;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("status_change_url")
    private String statusChangeUrl;

    @JsonProperty("is_3ds_enabled")
    private boolean threeDSEnabled;

    @JsonProperty("amex_mid")
    private String amexMid;

    @JsonProperty("facial_auth_enabled")
    private boolean facialAuthEnabled;

    @CreationTimestamp
    private LocalDateTime recordTimestamp;

    public CheckoutCompletedAutomation() {}

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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    public boolean isTestModeEnabled() {
        return testModeEnabled;
    }

    public void setTestModeEnabled(boolean testModeEnabled) {
        this.testModeEnabled = testModeEnabled;
    }

    public boolean isInternationalPaymentEnabled() {
        return internationalPaymentEnabled;
    }

    public void setInternationalPaymentEnabled(boolean internationalPaymentEnabled) {
        this.internationalPaymentEnabled = internationalPaymentEnabled;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getStatusChangeUrl() {
        return statusChangeUrl;
    }

    public void setStatusChangeUrl(String statusChangeUrl) {
        this.statusChangeUrl = statusChangeUrl;
    }

    public boolean isThreeDSEnabled() {
        return threeDSEnabled;
    }

    public void setThreeDSEnabled(boolean threeDSEnabled) {
        this.threeDSEnabled = threeDSEnabled;
    }

    public String getAmexMid() {
        return amexMid;
    }

    public void setAmexMid(String amexMid) {
        this.amexMid = amexMid;
    }

    public boolean isFacialAuthEnabled() {
        return facialAuthEnabled;
    }

    public void setFacialAuthEnabled(boolean facialAuthEnabled) {
        this.facialAuthEnabled = facialAuthEnabled;
    }

    public LocalDateTime getRecordTimestamp() {
        return recordTimestamp;
    }

    public void setRecordTimestamp(LocalDateTime recordTimestamp) {
        this.recordTimestamp = recordTimestamp;
    }
}
