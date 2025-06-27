package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.repository;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.model.Merchant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    boolean existsByEstablishmentCode(String establishmentCode);
}
