package org.example.frauddetection.domain.fraud.rules;

import org.example.frauddetection.domain.model.FraudRuleResult;
import org.example.frauddetection.dtos.TransactionRequest;
import org.springframework.stereotype.Service;

@Service
public class CardUsageFrequencyRule implements FraudRule {

    @Override
    public FraudRuleResult evaluate(TransactionRequest request) {
        return new FraudRuleResult(
                false,
                false,
               null,
                30
        );
    }
}
