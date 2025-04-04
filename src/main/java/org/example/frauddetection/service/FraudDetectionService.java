package org.example.frauddetection.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.frauddetection.domain.fraud.rules.AmountThresholdRule;
import org.example.frauddetection.domain.fraud.rules.CardUsageFrequencyRule;
import org.example.frauddetection.domain.fraud.rules.UnknownTerminalRule;
import org.example.frauddetection.domain.model.FraudRuleResultWithWeight;
import org.example.frauddetection.domain.model.FraudRuleWithWeight;
import org.example.frauddetection.dtos.FraudDetectionResponse;
import org.example.frauddetection.dtos.TransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final static int fraudScoreThreshold = 40;

    @PostConstruct
    private void postConstruct() {
        this.fraudRulesWithWeight = List.of(
                new FraudRuleWithWeight(amountThresholdRule, 0.2),
                new FraudRuleWithWeight(cardUsageFrequencyRule, 0.3),
                new FraudRuleWithWeight(unknownTerminalRule, 0.5)
        );
    }

    public FraudDetectionResponse detectFraud(TransactionRequest request) {
        List<FraudRuleResultWithWeight> fraudRuleResults =
                fraudRulesWithWeight.stream().map(ruleWithWeight ->
                        new FraudRuleResultWithWeight(ruleWithWeight.fraudRule().evaluate(request), ruleWithWeight.weight()))
                        .filter(fraudRuleResultWithWeight -> !fraudRuleResultWithWeight.ruleResult().skipped()).toList();

        // Calculate fraud score as a weighted result of all risks
        int fraudScore = fraudRuleResults.stream().mapToInt(ruleWithWeight -> (int) (ruleWithWeight.ruleResult().riskScore()*ruleWithWeight.weight())).sum();

        // If any of the fraud rules returns a direct rejection we reject the request
        List<FraudRuleResultWithWeight> rejectedFraudRules = fraudRuleResults.stream().filter(result -> result.ruleResult().rejected()).toList();
        if(!rejectedFraudRules.isEmpty()) {
            log.info("Detected fraud for transaction {}. Fraud rules resulting in rejecting are: {}", request.toString(), rejectedFraudRules);
            FraudRuleResultWithWeight firstRejectedRule = rejectedFraudRules.get(0);
            return new FraudDetectionResponse(true, firstRejectedRule.ruleResult().rejectionMessage(), fraudScore);
        }

        // Otherwise we determine if fraud score exceeds our threshold value
        if(fraudScore >= fraudScoreThreshold) {
            log.info("Rejected transaction {}. fraud score was: {}", request.toString(), fraudScore);
            return new FraudDetectionResponse(true, "Fraud score too high", fraudScore);
        }

        // If all checks passes we allow request
        log.info("Allowed transaction {}. fraud score was: {}", request.toString(), fraudScore);
        return new FraudDetectionResponse(false, null, fraudScore);
    }

}
