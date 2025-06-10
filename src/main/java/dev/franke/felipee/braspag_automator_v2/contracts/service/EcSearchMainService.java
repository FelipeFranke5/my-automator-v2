package dev.franke.felipee.braspag_automator_v2.contracts.service;

import java.util.List;

public interface EcSearchMainService {
    List<?> findAll();

    List<?> jsonOutput();

    void clear();

    void save(Object object);
}
