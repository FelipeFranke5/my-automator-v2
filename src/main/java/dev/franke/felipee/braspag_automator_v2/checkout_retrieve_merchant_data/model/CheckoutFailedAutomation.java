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
public class CheckoutFailedAutomation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("numeroEc")
    private String ecNumber;

    @JsonProperty("mensagem")
    private String message;

    @JsonProperty("dataRegistro")
    @CreationTimestamp
    private LocalDateTime recordTimestamp;

    public CheckoutFailedAutomation() {}

    public CheckoutFailedAutomation(final String ecNumber, final String message) {
        this.ecNumber = ecNumber;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getEcNumber() {
        return ecNumber;
    }

    public void setEcNumber(final String ecNumber) {
        this.ecNumber = ecNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public LocalDateTime getRecordTimestamp() {
        return recordTimestamp;
    }

    public void setRecordTimestamp(final LocalDateTime recordTimestamp) {
        this.recordTimestamp = recordTimestamp;
    }
}
