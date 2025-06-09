package dev.franke.felipee.braspag_automator_v2.contracts.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;

public interface EcSearchMainController {
    ResponseEntity<Void> executeAutomation(String[] ecs, String header);

    ResponseEntity<List<?>> getResultsInJson(String header);

    ResponseEntity<Void> deleteResults(String header);
}
