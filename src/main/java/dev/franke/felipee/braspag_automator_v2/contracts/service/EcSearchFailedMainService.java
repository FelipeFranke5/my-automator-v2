package dev.franke.felipee.braspag_automator_v2.contracts.service;

import java.util.List;

public interface EcSearchFailedMainService {
    void save(String ecNumber, String message);

    List<?> jsonOutput();

    void deleteAll();
}
