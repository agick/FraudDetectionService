package org.example.frauddetection.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.frauddetection.dtos.FraudDetectionResponse;
import org.example.frauddetection.dtos.TransactionRequest;
import org.example.frauddetection.service.FraudDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    @PostMapping("/check")
    public ResponseEntity<FraudDetectionResponse> checkFraud(@RequestBody @Valid TransactionRequest request) {
        return ResponseEntity.ok(fraudDetectionService.detectFraud(request));
    }
}
