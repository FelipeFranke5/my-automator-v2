package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.dto.ResultOutput;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.model.Enable3DSResult;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.repository.Enable3DSResultRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Enable3DSResultService {

    private static final Logger LOG = LoggerFactory.getLogger(Enable3DSResultService.class);

    private final Enable3DSResultRepository enable3dsResultRepository;

    public Enable3DSResultService(Enable3DSResultRepository enable3dsResultRepository) {
        this.enable3dsResultRepository = enable3dsResultRepository;
    }

    private boolean resultIsValid(String result) {
        if (result == null) return false;
        return !result.isBlank();
    }

    private boolean ecIsValid(String ec) {
        if (ec == null) return false;
        if (ec.isBlank()) return false;
        if (ec.length() != 10) return false;

        Optional<Enable3DSResult> failOptional = enable3dsResultRepository.findByEc(ec);

        if (failOptional.isPresent()) return false;

        try {
            return Long.parseLong(ec) > 0;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }

    public void save(String ec, String result) {
        LOG.info("Called to save automation result");
        if (ecIsValid(ec) && resultIsValid(result)) {
            try {
                LOG.info("Trying to save");
                Enable3DSResult enableResult = new Enable3DSResult(ec, result);
                enable3dsResultRepository.save(enableResult);
                LOG.info("Saved");
            } catch (Exception exception) {
                LOG.warn("Unable to Save!");
                LOG.error("Error to Save due to exception..", exception);
            }
        } else {
            LOG.warn("Not saving result, because EC or result is not valid..");
        }
    }

    public String resultsInString() {
        LOG.info("Called - Results in String");
        LOG.info("Max Results allowed = 100");
        List<Enable3DSResult> results = allResults();

        if (results.isEmpty()) return "";
        if (results.size() > 100) return "POSSUI MAIS DE 100 REGISTROS! LIMPE OS REGISTROS OU UTILIZE A LISTA EM JSON";

        StringBuilder builder = new StringBuilder();

        results.forEach(result ->
                builder.append("\n").append(result.getEc()).append("     ").append(result.getResult()));

        return builder.toString();
    }

    public List<ResultOutput> resultsJson() {
        return allResults().stream()
                .map(res -> new ResultOutput(res.getEc(), res.getResult()))
                .toList();
    }

    public void deleteAll() {
        enable3dsResultRepository.deleteAll();
    }

    public boolean existsByEc(String ec) {
        return enable3dsResultRepository.existsByEc(ec);
    }

    private List<Enable3DSResult> allResults() {
        return enable3dsResultRepository.findAll();
    }
}
