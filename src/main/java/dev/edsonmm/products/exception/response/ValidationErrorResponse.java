package dev.edsonmm.products.exception.response;

import java.util.Map;

public record ValidationErrorResponse(int status, Map<String, String> errors) {}
