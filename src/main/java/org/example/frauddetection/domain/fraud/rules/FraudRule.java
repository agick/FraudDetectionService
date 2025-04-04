package org.example.frauddetection.domain.fraud.rules;

import org.example.frauddetection.domain.model.FraudRuleResult;
import org.example.frauddetection.dtos.TransactionRequest;

public interface FraudRule {
    FraudRuleResult evaluate(TransactionRequest request);
}
