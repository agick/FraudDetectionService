package org.example.frauddetection.domain.fraud.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.example.frauddetection.domain.model.FraudRuleResult;
import org.example.frauddetection.dtos.TransactionRequest;
import org.example.frauddetection.repository.PaymentTerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class AmountThresholdRuleTest {

    @Mock
    private PaymentTerminalRepository paymentTerminalRepository;

    @InjectMocks
    private AmountThresholdRule amountThresholdRule;

    private TransactionRequest createRequest(double amount, int terminalThreatScore, String terminalId, String currency) {
        return new TransactionRequest(amount, currency, terminalId, terminalThreatScore, "");
    }

    @Test
    public void whenThresholdNotSet_shouldReturnSuccess() {
        TransactionRequest request = createRequest(100, 0, "terminal1", "USD");
        when(paymentTerminalRepository.getMaxAmountThresholdForPaymentTerminalAndCurrency("terminal1", "USD")).thenReturn(Optional.empty());

        FraudRuleResult result = amountThresholdRule.evaluate(request);

        assertTrue(result.skipped());
        assertFalse(result.rejected());
        assertNull(result.rejectionMessage());
        assertEquals(0, result.riskScore());
    }

    @Test
    public void whenAmountExceedsThreshold_shouldReturnFraud() {
        TransactionRequest request = createRequest(200, 10, "terminal2", "EUR");
        when(paymentTerminalRepository.getMaxAmountThresholdForPaymentTerminalAndCurrency("terminal2", "EUR")).thenReturn(Optional.of(100.0));

        FraudRuleResult result = amountThresholdRule.evaluate(request);

        assertFalse(result.skipped());
        assertTrue(result.rejected());
        assertEquals("Amount too high", result.rejectionMessage());
        assertEquals(100, result.riskScore());
    }

}
