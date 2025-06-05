package dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.repository;

import dev.franke.felipee.braspag_automator_v2.checkout_retrieve_merchant_data.model.CheckoutCompletedAutomation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckoutCompletedAutomationRepository extends JpaRepository<CheckoutCompletedAutomation, UUID> {}
