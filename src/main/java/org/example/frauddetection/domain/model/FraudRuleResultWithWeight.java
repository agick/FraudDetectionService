package org.example.frauddetection.domain.model;

public record FraudRuleResultWithWeight(FraudRuleResult ruleResult, double weight)  {
}
