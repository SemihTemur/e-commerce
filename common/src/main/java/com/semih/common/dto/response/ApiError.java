package com.semih.common.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiError(int status, String message, OffsetDateTime  timestamp, Map<String,String> errors) {
}

