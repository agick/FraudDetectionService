package org.example.frauddetection.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @Min(0)
    private double amount;
    @NotBlank
    private String currency;
    @NotBlank
    private String terminalId;
    @Min(0)
    @Max(100)
    private int terminalThreatScore;
    @NotBlank
    @Size(min = 16, max = 16)
    private String cardNumber;

    @Override
    public String toString() {
        return "TransactionRequest [amount=" + amount + ", currency=" + currency + ", terminalId= " + terminalId + ", terminalThreatScore=" + terminalThreatScore + ", cardNumber=************" + (cardNumber != null && cardNumber.length() == 16 ? cardNumber.substring(12, 16) : "Unprocessable") + "]";
    }
}

