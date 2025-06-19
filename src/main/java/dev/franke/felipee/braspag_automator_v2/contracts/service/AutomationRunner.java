package dev.franke.felipee.braspag_automator_v2.contracts.service;

import java.util.Optional;

public interface AutomationRunner {
    Optional<?> singleEcRoutine(String ecNumber);
}
