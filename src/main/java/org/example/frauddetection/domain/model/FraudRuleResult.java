package org.example.frauddetection.domain.model;

public record FraudRuleResult(boolean skipped, boolean rejected, String rejectionMessage, int riskScore) {
}
