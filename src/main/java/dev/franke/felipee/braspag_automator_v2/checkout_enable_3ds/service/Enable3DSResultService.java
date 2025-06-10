package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.dto.ResultOutput;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.model.Enable3DSResult;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.repository.Enable3DSResultRepository;
import dev.franke.felipee.braspag_automator_v2.contracts.service.EcSearchMainService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Enable3DSResultService implements EcSearchMainService {

    private static final Logger LOG = LoggerFactory.getLogger(Enable3DSResultService.class);

    private final Enable3DSResultRepository enable3dsResultRepository;

    public Enable3DSResultService(Enable3DSResultRepository enable3dsResultRepository) {
        this.enable3dsResultRepository = enable3dsResultRepository;
    }

    @Override
    public List<Enable3DSResult> findAll() {
        return enable3dsResultRepository.findAll();
    }

    @Override
    public List<ResultOutput> jsonOutput() {
        return findAll().stream()
                .map(res -> new ResultOutput(res.getEc(), res.getResult()))
                .toList();
    }

    @Override
    public void clear() {
        enable3dsResultRepository.deleteAll();
    }

    @Override
    public void save(Object result) {
        enable3dsResultRepository.save((Enable3DSResult) result);
    }

    public void save(String ec, String result) {
        LOG.info("Called to save automation result");
        if (ecIsValid(ec) && resultIsValid(result)) {
            try {
                LOG.info("Trying to save");
                Enable3DSResult enableResult = new Enable3DSResult(ec, result);
                save(enableResult);
                LOG.info("Saved");
            } catch (Exception exception) {
                LOG.warn("Unable to Save!");
                LOG.error("Error to Save due to exception..", exception);
            }
        } else {
            LOG.warn("Not saving result, because EC or result is not valid..");
        }
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

    public String resultsInString() {
        LOG.info("Called - Results in String");
        LOG.info("Max Results allowed = 100");
        List<Enable3DSResult> results = findAll();

        if (results.isEmpty()) return "";
        if (results.size() > 100) return "POSSUI MAIS DE 100 REGISTROS! LIMPE OS REGISTROS OU UTILIZE A LISTA EM JSON";

        StringBuilder builder = new StringBuilder();

        results.forEach(result ->
                builder.append("\n").append(result.getEc()).append("     ").append(result.getResult()));

        return builder.toString();
    }

    public boolean existsByEc(String ec) {
        return enable3dsResultRepository.existsByEc(ec);
    }
}
