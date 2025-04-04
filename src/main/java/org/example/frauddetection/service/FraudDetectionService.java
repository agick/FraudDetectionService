package org.example.frauddetection.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.frauddetection.config.FraudDetectionConstants;
import org.example.frauddetection.domain.fraud.rules.AmountThresholdRule;
import org.example.frauddetection.domain.fraud.rules.CardUsageFrequencyRule;
import org.example.frauddetection.domain.fraud.rules.UnknownTerminalRule;
import org.example.frauddetection.domain.model.FraudRuleResultWithWeight;
import org.example.frauddetection.domain.model.FraudRuleWithWeight;
import org.example.frauddetection.dtos.FraudDetectionResponse;
import org.example.frauddetection.dtos.TransactionRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {

    // Fraud rules
    private final AmountThresholdRule amountThresholdRule;
    private final CardUsageFrequencyRule cardUsageFrequencyRule;
    private final UnknownTerminalRule unknownTerminalRule;

    private List<FraudRuleWithWeight> fraudRulesWithWeight;

    @PostConstruct
    void postConstruct() {
        // Ensure that all weights sum to 1.0
        this.fraudRulesWithWeight = List.of(
                new FraudRuleWithWeight(amountThresholdRule, 0.4),
                new FraudRuleWithWeight(cardUsageFrequencyRule, 0.25),
                new FraudRuleWithWeight(unknownTerminalRule, 0.35)
        );
    }

    public FraudDetectionResponse detectFraud(TransactionRequest request) {
        // Evaluate all rules for transaction
        List<FraudRuleResultWithWeight> fraudRuleResults =
                fraudRulesWithWeight.stream().map(ruleWithWeight ->
                        new FraudRuleResultWithWeight(ruleWithWeight.fraudRule().evaluate(request), ruleWithWeight.weight()))
                        .filter(fraudRuleResultWithWeight -> !fraudRuleResultWithWeight.ruleResult().skipped()).toList();

        // If all checks were skipped allow transaction
        if(fraudRuleResults.isEmpty()) {
            return new FraudDetectionResponse(false, null, 0);
        }

        // Calculate weight multiplier to balance for skipped checks
        double weightMultiplier = 1.0/fraudRuleResults.stream().mapToDouble(FraudRuleResultWithWeight::weight).sum();

        // Calculate fraud score as a weighted result of all risks
        int fraudScore = (int) fraudRuleResults.stream().mapToDouble(ruleWithWeight -> (ruleWithWeight.ruleResult().riskScore()*ruleWithWeight.weight()*weightMultiplier)).sum();

        // If any of the fraud rules returns a direct rejection we reject the request
        List<FraudRuleResultWithWeight> rejectedFraudRules = fraudRuleResults.stream().filter(result -> result.ruleResult().rejected()).toList();
        if(!rejectedFraudRules.isEmpty()) {
            log.warn("Detected fraud for transaction {}. Fraud rules resulting in rejecting are: {}", request.toString(), rejectedFraudRules);
            FraudRuleResultWithWeight firstRejectedRule = rejectedFraudRules.get(0);
            return new FraudDetectionResponse(true, firstRejectedRule.ruleResult().rejectionMessage(), fraudScore);
        }

        // Otherwise we determine if fraud score exceeds our threshold value
        if(fraudScore >= FraudDetectionConstants.FRAUD_SCORE_REJECT_THRESHOLD) {
            log.info("Rejected transaction {}. fraud score was: {}", request.toString(), fraudScore);
            return new FraudDetectionResponse(true, FraudDetectionConstants.FRAUD_SCORE_TOO_HIGH_REJECTION_MESSAGE, fraudScore);
        }

        // If all checks passes we allow request
        log.info("Allowed transaction {}. fraud score was: {}", request.toString(), fraudScore);
        return new FraudDetectionResponse(false, null, fraudScore);
    }

}
