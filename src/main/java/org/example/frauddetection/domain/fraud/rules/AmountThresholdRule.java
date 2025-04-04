package org.example.frauddetection.domain.fraud.rules;

import lombok.RequiredArgsConstructor;
import org.example.frauddetection.domain.model.FraudRuleResult;
import org.example.frauddetection.dtos.TransactionRequest;
import org.example.frauddetection.repository.PaymentTerminalRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AmountThresholdRule implements FraudRule {

    private final PaymentTerminalRepository paymentTerminalRepository;

    @Override
    public FraudRuleResult evaluate(TransactionRequest request) {
        Optional<Double> highestValidAmountForTerminal = paymentTerminalRepository.getMaxAmountThresholdForPaymentTerminalAndCurrency(request.getTerminalId(), request.getCurrency());

        // If this terminal does not have threshold set for this currency
        if(highestValidAmountForTerminal.isEmpty()) {
            return new FraudRuleResult(
                    true,
                    false,
                    null,
                    0
            );
        }

        // Terminal threat score reduces the threshold for the highest amount possible for a transaction
        double highestValidAmountForTransaction = highestValidAmountForTerminal.get() - (((highestValidAmountForTerminal.get() * 0.9) * 0.01) * request.getTerminalThreatScore());

        boolean exceedsValidAmount = request.getAmount() > highestValidAmountForTransaction;
        return new FraudRuleResult(
                false,
                exceedsValidAmount,
                exceedsValidAmount ? "Amount too high" : null,
                exceedsValidAmount ? 100 : (int) (request.getAmount() / highestValidAmountForTransaction) * 100
        );
    }
}
