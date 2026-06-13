package com.regensburg.paymentservice.mock.adapter;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * A simplified mock representation of an ISO 8583 message.
 * In a real scenario, this would involve complex bitmapping, field parsing,
 * and serialization using a dedicated ISO 8583 library.
 */
@Value
@Builder
public class Iso8583Message {
    String mti; // Message Type Indicator, e.g., "0200"
    String primaryBitmap; // Simplified, in real life it's a bitmask
    String processingCode; // Field 3
    BigDecimal transactionAmount; // Field 4
    String transmissionDateTime; // Field 7, e.g., "MMDDhhmmss"
    String systemTraceAuditNumber; // Field 11
    String localTransactionTime; // Field 12
    String localTransactionDate; // Field 13
    String acquiringInstitutionIdCode; // Field 32
    String forwardingInstitutionIdCode; // Field 33
    String primaryAccountNumber; // Field 2
    String terminalId; // Field 41
    String cardAcceptorNameLocation; // Field 43
    String currencyCode; // Field 49
    String responseCode; // Field 39, e.g., "00" for approved
}
