package com.vbank.transactionservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    private String message;
    private String messageType;
    private String dateTime;
}
