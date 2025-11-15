package com.semih.common.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(int status, String message, LocalDateTime timestamp, Map<String,String> errors) {
}

