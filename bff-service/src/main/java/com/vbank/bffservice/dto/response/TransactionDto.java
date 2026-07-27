package com.vbank.bffservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * ASSUMED CONTRACT - see AccountDto's note; same caveat applies.
 *
 * SPEC INCONSISTENCY WORTH FLAGGING: the project spec's documented
 * GET /accounts/{accountId}/transactions example includes
 * fromAccountId, toAccountId, amount, description, timestamp, AND
 * deliveryStatus - but the spec's dashboard aggregation example
 * (section 4, BFF) shows a narrower shape per transaction: just
 * transactionId, amount, toAccountId, description, timestamp. It's
 * unclear whether that's a deliberate trim for the dashboard view or
 * just an inconsistency in the doc.
 *
 * DECISION MADE HERE: this DTO keeps the full field set from the
 * transaction-history endpoint (more informative, and it's what the
 * real endpoint actually returns), and the dashboard response simply
 * includes all of it rather than trimming down to match the
 * narrower dashboard example. If the frontend/BFF consumer needs the
 * narrower shape specifically, that's a one-line change in the
 * mapper - flagging this as a decision to confirm, not silently
 * picking a side.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private UUID transactionId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String description;
    private Instant timestamp;
    private String deliveryStatus;
}
