package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class Enable3DSResult {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID recordId;

  private String ec;

  private String result;

  @CreationTimestamp private LocalDateTime recordTimestamp;

  public Enable3DSResult() {}

  public Enable3DSResult(String ec, String result) {
    this.ec = ec;
    this.result = result;
  }

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

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public LocalDateTime getRecordTimestamp() {
    return recordTimestamp;
  }

  public void setRecordTimestamp(LocalDateTime recordTimestamp) {
    this.recordTimestamp = recordTimestamp;
  }
}
