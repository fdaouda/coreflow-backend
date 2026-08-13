package com.coreflow.invoice.controller;

import com.coreflow.common.CloudWatchAuditService;
import com.coreflow.invoice.service.InvoiceStorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    private final InvoiceStorageService invoiceStorageService;
    private final CloudWatchAuditService auditService;

    public InvoiceController(InvoiceStorageService invoiceStorageService, CloudWatchAuditService auditService) {
        this.invoiceStorageService = invoiceStorageService;
        this.auditService = auditService;
    }

    @PostMapping("/{id}")
    @Operation(summary = "Générer et stocker la facture d'une commande sur S3")
    public ResponseEntity<String> generateInvoice(@PathVariable Long id) {
        String invoiceContent = "FACTURE COREFLOW - COMMANDE #" + id;
        String s3Key = "invoices/2026/facture-order-" + id + ".pdf";

        // 1. Stockage S3
        invoiceStorageService.uploadInvoice(s3Key, invoiceContent.getBytes(), "application/pdf");

        // 2. Audit CloudWatch
        String logMessage = String.format("{\"event\": \"INVOICE_GENERATED\", \"orderId\": %d, \"s3Key\": \"%s\"}", id, s3Key);
        auditService.sendAuditLog(logMessage);

        return ResponseEntity.ok("Facture stockée sur S3 et événement audité dans CloudWatch !");
    }
}
