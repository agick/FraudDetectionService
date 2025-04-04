package org.example.frauddetection.dtos;

public record FraudDetectionResponse(boolean rejected, String rejectionMessage, int fraudScore) {
}
