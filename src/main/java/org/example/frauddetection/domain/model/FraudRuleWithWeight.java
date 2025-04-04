package org.example.frauddetection.domain.model;

import org.example.frauddetection.domain.fraud.rules.FraudRule;

public record FraudRuleWithWeight(FraudRule fraudRule, double weight)  {
}
