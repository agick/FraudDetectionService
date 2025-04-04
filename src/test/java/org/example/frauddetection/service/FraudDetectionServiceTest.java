package org.example.frauddetection.service;

import org.example.frauddetection.config.FraudDetectionConstants;
import org.example.frauddetection.domain.fraud.rules.AmountThresholdRule;
import org.example.frauddetection.domain.fraud.rules.CardUsageFrequencyRule;
import org.example.frauddetection.domain.fraud.rules.UnknownTerminalRule;
import org.example.frauddetection.domain.model.FraudRuleResult;
import org.example.frauddetection.dtos.FraudDetectionResponse;
import org.example.frauddetection.dtos.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FraudDetectionServiceTest {

    @Mock
    private AmountThresholdRule amountThresholdRule;
    @Mock
    private CardUsageFrequencyRule cardUsageFrequencyRule;
    @Mock
    private UnknownTerminalRule unknownTerminalRule;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        fraudDetectionService.postConstruct();
        transactionRequest = new TransactionRequest();
    }

    @Test
    void testNoRulesTriggered() {
        when(amountThresholdRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(false, false, null, 0));
        when(cardUsageFrequencyRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(false, false, null, 0));
        when(unknownTerminalRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(false, false, null, 0));

        FraudDetectionResponse response = fraudDetectionService.detectFraud(transactionRequest);

        assertFalse(response.rejected());
        assertNull(response.rejectionMessage());
        assertEquals(0, response.fraudScore());
    }

    @Test
    void testAllRulesSkipped() {
        when(amountThresholdRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(true, false, null, 0));
        when(cardUsageFrequencyRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(true, false, null, 0));
        when(unknownTerminalRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(true, false, null, 0));

        FraudDetectionResponse response = fraudDetectionService.detectFraud(transactionRequest);

        assertFalse(response.rejected());
        assertNull(response.rejectionMessage());
        assertEquals(0, response.fraudScore());
    }

    @Test
    void testFraudScoreExceedsThreshold() {
        when(amountThresholdRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(false, false, null, FraudDetectionConstants.FRAUD_SCORE_REJECT_THRESHOLD));
        when(cardUsageFrequencyRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(false, false, null, FraudDetectionConstants.FRAUD_SCORE_REJECT_THRESHOLD));
        when(unknownTerminalRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(false, false, null, FraudDetectionConstants.FRAUD_SCORE_REJECT_THRESHOLD));

        FraudDetectionResponse response = fraudDetectionService.detectFraud(transactionRequest);

        assertTrue(response.rejected());
        assertEquals(FraudDetectionConstants.FRAUD_SCORE_TOO_HIGH_REJECTION_MESSAGE, response.rejectionMessage());
        assertTrue(response.fraudScore() >= 40);
    }

    @Test
    void testDirectRejection() {
        when(amountThresholdRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(false, true, "Direct rejection", 100));
        when(cardUsageFrequencyRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(false, false, null, FraudDetectionConstants.FRAUD_SCORE_REJECT_THRESHOLD));
        when(unknownTerminalRule.evaluate(transactionRequest)).thenReturn(new FraudRuleResult(false, false, null, FraudDetectionConstants.FRAUD_SCORE_REJECT_THRESHOLD));

        FraudDetectionResponse response = fraudDetectionService.detectFraud(transactionRequest);

        assertTrue(response.rejected());
        assertEquals("Direct rejection", response.rejectionMessage());
    }

}