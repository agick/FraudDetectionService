package org.example.frauddetection.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentTerminalRepository {

    public Optional<Double> getMaxAmountThresholdForPaymentTerminalAndCurrency(String paymentTerminalId, String currency) {
        // Returns max allowed amount. Should be based on terminal threat score in real example
        return Optional.of(20000.0);
    }

}
