package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.controller.filter;

import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.exception.InvalidFilterByException;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.MerchantService;
import dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.ProductFilterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class APIMerchantFilter {

    private static final String MERCHANT_BLOCKED_PARAM = "merchantBlocked";
    private static final String PIX_ENABLED_PARAM = "pixEnabled";
    private static final String AF_ENABLED_PARAM = "antifraudEnabled";
    private static final String TOKENIZATION_ENABLED_PARAM = "tokenizationEnabled";
    private static final String VELOCITY_ENABLED_PARAM = "velocityEnabled";
    private static final String RECURRENCY_ENABLED_PARAM = "smartRecurrencyEnabled";
    private static final String ZERO_AUTH_ENABLED_PARAM = "zeroAuthEnabled";
    private static final String BIN_QUERY_ENABLED_PARAM = "binQueryEnabled";
    private static final String SELECTIVE_AUTH_ENABLED_PARAM = "selectiveAuthEnabled";
    private static final String AUTO_CANCELLATION_ENABLED_PARAM = "automaticCancellationEnabbled";
    private static final String FORCE_BP_AUTH_ENABLED_PARAM = "forceBraspagAuthEnabled";
    private static final String MTLS_ENABLED_PARAM = "mtlsEnabled";
    private static final String WEBHOOK_ENABLED_PARAM = "webhookEnabled";
    private static final String ONE_IP_PARAM = "atLeastOneIpEnabled";

    @Autowired
    private MerchantService merchantService;

    public String applyFilter(String filterBy) {
        return switch (filterBy) {
            case MERCHANT_BLOCKED_PARAM -> merchantService.getTextResult(ProductFilterType.MERCHANT_BLOCKED);
            case PIX_ENABLED_PARAM -> merchantService.getTextResult(ProductFilterType.PIX_ENABLED);
            case AF_ENABLED_PARAM -> merchantService.getTextResult(ProductFilterType.ANTIFRAUD_ENABLED);
            case TOKENIZATION_ENABLED_PARAM -> merchantService.getTextResult(ProductFilterType.TOKENIZATION_ENABLED);
            case VELOCITY_ENABLED_PARAM -> merchantService.getTextResult(ProductFilterType.VELOCITY_ENABLED);
            case RECURRENCY_ENABLED_PARAM -> merchantService.getTextResult(ProductFilterType.SMART_RECURRENCY_ENABLED);
            case ZERO_AUTH_ENABLED_PARAM -> merchantService.getTextResult(ProductFilterType.ZERO_AUTH_ENABLED);
            case BIN_QUERY_ENABLED_PARAM -> merchantService.getTextResult(ProductFilterType.BIN_QUERY_ENABLED);
            case SELECTIVE_AUTH_ENABLED_PARAM -> merchantService.getTextResult(
                    ProductFilterType.SELECTIVE_AUTH_ENABLED);
            case AUTO_CANCELLATION_ENABLED_PARAM -> merchantService.getTextResult(
                    ProductFilterType.AUTOMATIC_CANCELLATION_ENABLED);
            case FORCE_BP_AUTH_ENABLED_PARAM -> merchantService.getTextResult(
                    ProductFilterType.FORCE_BRASPAG_AUTH_ENABLED);
            case MTLS_ENABLED_PARAM -> merchantService.getTextResult(ProductFilterType.MTLS_ENABLED);
            case WEBHOOK_ENABLED_PARAM -> merchantService.getTextResult(ProductFilterType.WEBHOOK_ENABLED);
            case ONE_IP_PARAM -> merchantService.getTextResult(ProductFilterType.AT_LEAST_ONE_IP);
            default -> merchantService.getTextResult(ProductFilterType.NO_FILTER);
        };
    }

    public void assertFilterByIsValid(String filterBy) {
        if (filterByIsNullOrBlank(filterBy)) {
            return;
        }
        if (!filterByIsValid(filterBy)) {
            throw new InvalidFilterByException("Invalid filter");
        }
    }

    public boolean filterByIsNullOrBlank(String filterBy) {
        return filterBy == null || filterBy.isBlank();
    }

    public boolean filterByIsValid(String filterBy) {
        return filterBy.equals(MERCHANT_BLOCKED_PARAM)
                || filterBy.equals(PIX_ENABLED_PARAM)
                || filterBy.equals(AF_ENABLED_PARAM)
                || filterBy.equals(TOKENIZATION_ENABLED_PARAM)
                || filterBy.equals(VELOCITY_ENABLED_PARAM)
                || filterBy.equals(RECURRENCY_ENABLED_PARAM)
                || filterBy.equals(ZERO_AUTH_ENABLED_PARAM)
                || filterBy.equals(BIN_QUERY_ENABLED_PARAM)
                || filterBy.equals(SELECTIVE_AUTH_ENABLED_PARAM)
                || filterBy.equals(AUTO_CANCELLATION_ENABLED_PARAM)
                || filterBy.equals(FORCE_BP_AUTH_ENABLED_PARAM)
                || filterBy.equals(MTLS_ENABLED_PARAM)
                || filterBy.equals(WEBHOOK_ENABLED_PARAM)
                || filterBy.equals(ONE_IP_PARAM);
    }
}
