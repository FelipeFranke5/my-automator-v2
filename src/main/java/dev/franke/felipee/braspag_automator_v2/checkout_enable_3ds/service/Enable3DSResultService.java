package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service;

import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.model.Enable3DSResult;
import dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.repository.Enable3DSResultRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Enable3DSResultService {

    private static final Logger LOG = LoggerFactory.getLogger(Enable3DSResultService.class);

    private Enable3DSResultRepository enable3dsResultRepository;
    private CheckoutMerchantValidator checkoutMerchantValidator;

    public Enable3DSResultService(
            Enable3DSResultRepository enable3dsResultRepository, CheckoutMerchantValidator checkoutMerchantValidator) {
        this.enable3dsResultRepository = enable3dsResultRepository;
        this.checkoutMerchantValidator = checkoutMerchantValidator;
    }

    public void save(String ec, String result) {
        LOG.info("Called to save automation result");
        String[] singleEcArray = {ec};

        if (!(checkoutMerchantValidator.allEcsAreValid(singleEcArray))) {
            LOG.warn("EC is not valid! Not saving..");
            return;
        }

        try {
            LOG.info("Trying to save");
            Enable3DSResult enableResult = new Enable3DSResult(ec, result);
            enable3dsResultRepository.save(enableResult);
            LOG.info("Saved");
        } catch (Exception exception) {
            LOG.warn("Unable to Save!");
            LOG.error("Error to Save due to exception..", exception);
        }
    }

    public String resultsInString() {
        List<Enable3DSResult> results = allResults();
        StringBuilder builder = new StringBuilder();

        if (results.isEmpty()) return "";

        results.stream().forEach(result -> {
            builder.append("\n").append(result.getEc()).append("     ").append(result.getResult());
        });

        return builder.toString();
    }

    private List<Enable3DSResult> allResults() {
        return enable3dsResultRepository.findAll();
    }
}
