package dev.franke.felipee.braspag_automator_v2.contracts.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;

public interface EcSearchFailureMainController {
    ResponseEntity<List<?>> getAutomationsWithError(String header);

    ResponseEntity<Void> deleteAutomationsWithError(String header);
}
