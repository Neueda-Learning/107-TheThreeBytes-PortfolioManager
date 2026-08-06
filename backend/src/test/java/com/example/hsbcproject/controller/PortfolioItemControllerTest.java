package com.example.hsbcproject.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.dto.PortfolioItemResponse;
import com.example.hsbcproject.dto.PortfolioSummaryResponse;
import com.example.hsbcproject.dto.TransactionResponse;
import com.example.hsbcproject.dto.CreatePortfolioItemRequest;
import com.example.hsbcproject.dto.UpdatePortfolioItemRequest;
import com.example.hsbcproject.dto.SellHoldingRequest;
import com.example.hsbcproject.service.PortfolioItemService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
class PortfolioItemControllerTest {
    @Test
    void getAllDelegatesToService() {
        PortfolioItemService service = mock(PortfolioItemService.class);
        when(service.findAll()).thenReturn(List.of(new PortfolioItemResponse(1L, "AAPL", new BigDecimal("10.00000000"), AssetType.STOCK, new BigDecimal("100.00"), LocalDate.now(), null, null, null, null, null)));
        PortfolioItemController controller = new PortfolioItemController(service);
        List<PortfolioItemResponse> result = controller.getAll();
        assertEquals(1, result.size());
        verify(service).findAll();
    }
    @Test
    void createReturnsCreatedResponse() {
        PortfolioItemService service = mock(PortfolioItemService.class);
        CreatePortfolioItemRequest request = new CreatePortfolioItemRequest("AAPL", new BigDecimal("1.00000000"), AssetType.STOCK, new BigDecimal("100.00"), LocalDate.now(), null, null, null, null, null);
        when(service.create(request)).thenReturn(new PortfolioItemResponse(9L, "AAPL", new BigDecimal("1.00000000"), AssetType.STOCK, new BigDecimal("100.00"), LocalDate.now(), null, null, null, null, null));
        PortfolioItemController controller = new PortfolioItemController(service);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/portfolio-items");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
        var response = controller.create(request);
        assertEquals(201, response.getStatusCode().value());
        assertEquals("AAPL", response.getBody().ticker());
        RequestContextHolder.resetRequestAttributes();
    }
    @Test
    void sellDelegatesToService() {
        PortfolioItemService service = mock(PortfolioItemService.class);
        when(service.sell(1L, new BigDecimal("150.00"), new BigDecimal("2.00000000")))
                .thenReturn(new TransactionResponse(1L, "AAPL", AssetType.STOCK, com.example.hsbcproject.domain.TransactionType.SELL, new BigDecimal("2.00000000"), new BigDecimal("150.00"), new BigDecimal("300.0000000000"), LocalDate.now(), null));
        PortfolioItemController controller = new PortfolioItemController(service);
        var result = controller.sell(1L, new SellHoldingRequest(new BigDecimal("150.00"), new BigDecimal("2.00000000")));
        assertEquals(new BigDecimal("300.0000000000"), result.totalValue());
        verify(service).sell(1L, new BigDecimal("150.00"), new BigDecimal("2.00000000"));
    }
    @Test
    void summaryDelegatesToService() {
        PortfolioItemService service = mock(PortfolioItemService.class);
        when(service.getSummary()).thenReturn(new PortfolioSummaryResponse(1, new BigDecimal("1.00000000"), new BigDecimal("100.00"), Map.of("STOCK", new BigDecimal("1.00000000")), Map.of("STOCK", new BigDecimal("100.00"))));
        PortfolioItemController controller = new PortfolioItemController(service);
        assertEquals(1, controller.summary().totalPositions());
    }
}
