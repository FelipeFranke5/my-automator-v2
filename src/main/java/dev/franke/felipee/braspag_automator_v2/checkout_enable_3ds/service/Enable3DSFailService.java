package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.dto.ResultOutput;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.model.Enable3DSFail;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.repository.Enable3DSFailRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class Enable3DSFailService {

    private final Enable3DSFailRepository repository;
    private final Enable3DSResultService successfulResultsService;

    public Enable3DSFailService(Enable3DSFailRepository repository, Enable3DSResultService successfulResultsService) {
        this.repository = repository;
        this.successfulResultsService = successfulResultsService;
    }

    private boolean resultIsValid(String result) {
        if (result == null) return false;
        return !result.isBlank();
    }

    private boolean ecIsValid(String ec) {
        if (ec == null) return false;
        if (ec.isBlank()) return false;
        if (ec.length() != 10) return false;

        Optional<Enable3DSFail> failOptional = repository.findByEc(ec);

        if (failOptional.isPresent()) return false;

        try {
            return Long.parseLong(ec) > 0 && !successfulResultsService.existsByEc(ec);
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }

    public void save(String ec, String result) {
        if (ecIsValid(ec) && resultIsValid(result)) {
            Enable3DSFail newFail = new Enable3DSFail();
            newFail.setEc(ec);
            newFail.setResult(result);
            repository.save(newFail);
        }
    }

    public List<ResultOutput> jsonOutput() {
        return listAll().stream()
                .map(res -> new ResultOutput(res.getEc(), res.getResult()))
                .toList();
    }

    private List<Enable3DSFail> listAll() {
        return repository.findAll();
    }

    public void removeAll() {
        repository.deleteAll();
    }
}
