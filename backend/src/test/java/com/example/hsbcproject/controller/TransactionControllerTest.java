package com.example.hsbcproject.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.TransactionType;
import com.example.hsbcproject.dto.CreateTransactionRequest;
import com.example.hsbcproject.dto.TransactionResponse;
import com.example.hsbcproject.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class TransactionControllerTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAllWithTickerUsesTickerSpecificServiceMethod() {
        TransactionService service = mock(TransactionService.class);
        when(service.findByTicker("AAPL")).thenReturn(List.of());
        TransactionController controller = new TransactionController(service);

        controller.getAll("AAPL");

        verify(service).findByTicker("AAPL");
    }

    @Test
    void getAllWithBlankTickerFallsBackToFindAll() {
        TransactionService service = mock(TransactionService.class);
        when(service.findAll()).thenReturn(List.of());
        TransactionController controller = new TransactionController(service);

        controller.getAll("   ");

        verify(service).findAll();
    }

    @Test
    void getByIdDelegatesToService() {
        TransactionService service = mock(TransactionService.class);
        TransactionResponse response = sampleResponse(7L);
        when(service.findById(7L)).thenReturn(response);
        TransactionController controller = new TransactionController(service);

        assertEquals(7L, controller.getById(7L).id());
    }

    @Test
    void createReturnsCreatedResponseAndLocationHeader() {
        TransactionService service = mock(TransactionService.class);
        when(service.create(sampleCreateRequest())).thenReturn(sampleResponse(9L));
        TransactionController controller = new TransactionController(service);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/transactions");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var response = controller.create(sampleCreateRequest());

        assertEquals(201, response.getStatusCode().value());
        assertEquals("http://localhost/api/transactions/9", response.getHeaders().getLocation().toString());
        assertEquals(9L, response.getBody().id());
    }

    @Test
    void deleteReturnsNoContentAndDelegatesToService() {
        TransactionService service = mock(TransactionService.class);
        TransactionController controller = new TransactionController(service);

        var response = controller.delete(15L);

        verify(service).delete(15L);
        assertEquals(204, response.getStatusCode().value());
    }

    private static CreateTransactionRequest sampleCreateRequest() {
        return new CreateTransactionRequest(
                "AAPL",
                AssetType.STOCK,
                TransactionType.BUY,
                new BigDecimal("2.00000000"),
                new BigDecimal("150.00"),
                LocalDate.now(),
                "test");
    }

    private static TransactionResponse sampleResponse(Long id) {
        return new TransactionResponse(
                id,
                "AAPL",
                AssetType.STOCK,
                TransactionType.BUY,
                new BigDecimal("2.00000000"),
                new BigDecimal("150.00"),
                new BigDecimal("300.00"),
                LocalDate.now(),
                "test");
    }
}
