package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.repository;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.model.Enable3DSResult;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Enable3DSResultRepository extends JpaRepository<Enable3DSResult, UUID> {
    Optional<Enable3DSResult> findByEc(String ec);
}
