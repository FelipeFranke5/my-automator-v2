package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service;

import dev.franke.felipe.api30_automation_api.automation.merchant_data.domain.CieloMerchant;
import dev.franke.felipe.api30_automation_api.automation.selenium.impl.AutomationRunnerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MerchantRunner {

    @Value("${braspag.prod.login}")
    private String login;

    @Value("${braspag.prod.password}")
    private String password;

    public CieloMerchant singleEcRoutine(String ecNumber) {
        AutomationRunnerImpl automationRunner = new AutomationRunnerImpl(login, password, ecNumber);
        return automationRunner.run();
    }
}
