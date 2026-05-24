package dev.edsonmm.products.exception.response;

public record ErrorResponse(int status, String message, String path) {}
