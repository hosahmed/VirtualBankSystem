package com.vbank.loggingservice.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LogMessageType {
    REQUEST("Request"),
    RESPONSE("Response");

    private final String wireValue;

    LogMessageType(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String getWireValue() {
        return wireValue;
    }

    @JsonCreator
    public static LogMessageType fromWireValue(String value) {
        for (LogMessageType type : values()) {
            if (type.wireValue.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown messageType: " + value);
    }
}
