package dev.franke.felipee.braspag_automator_v2.contracts.service;

import java.io.IOException;

public interface EcSearchFileHandler {
    void deleteJsonFileAfterAutomation(String ec);

    byte[] writeToExcelFile() throws IOException;
}
