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
    private String traceId;
    private String serviceName;
    private String method;
    private String uri;
    private String requestBody;
    private int responseStatus;
    private String responseBody;
    private long durationMs;
}
