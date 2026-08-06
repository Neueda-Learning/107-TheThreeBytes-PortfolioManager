package com.example.hsbcproject.controller;
import static org.junit.jupiter.api.Assertions.*;
import com.example.hsbcproject.exception.GlobalExceptionHandler;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    @Test
    void handleNotFoundReturns404() {
        var response = handler.handleNotFound(new ResourceNotFoundException("missing"));
        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not Found", response.getBody().error());
    }
    @Test
    void handleIllegalArgumentReturns400() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad request"));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Validation failed", response.getBody().error());
    }
}
