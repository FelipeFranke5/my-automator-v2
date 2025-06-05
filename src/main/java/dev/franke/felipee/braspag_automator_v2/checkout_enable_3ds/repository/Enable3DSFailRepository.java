package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.repository;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.model.Enable3DSFail;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Enable3DSFailRepository extends JpaRepository<Enable3DSFail, UUID> {
    Optional<Enable3DSFail> findByEc(String ec);
}
