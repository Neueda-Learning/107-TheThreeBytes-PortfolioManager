package com.example.hsbcproject.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.dto.CreateWatchlistItemRequest;
import com.example.hsbcproject.dto.WatchlistItemResponse;
import com.example.hsbcproject.service.WatchlistService;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class WatchlistControllerTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAllDelegatesToService() {
        WatchlistService service = mock(WatchlistService.class);
        when(service.findAll()).thenReturn(java.util.List.of());
        WatchlistController controller = new WatchlistController(service);

        controller.getAll();

        verify(service).findAll();
    }

    @Test
    void addReturnsCreatedResponseAndLocationHeader() {
        WatchlistService service = mock(WatchlistService.class);
        CreateWatchlistItemRequest request = new CreateWatchlistItemRequest("ETH", AssetType.CRYPTO);
        WatchlistItemResponse created = new WatchlistItemResponse(5L, "ETH", AssetType.CRYPTO, LocalDate.now());
        when(service.add(request)).thenReturn(created);
        WatchlistController controller = new WatchlistController(service);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/watchlist");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        var response = controller.add(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("http://localhost/api/watchlist/5", response.getHeaders().getLocation().toString());
        assertEquals(5L, response.getBody().id());
    }

    @Test
    void removeDelegatesToServiceAndReturnsNoContent() {
        WatchlistService service = mock(WatchlistService.class);
        WatchlistController controller = new WatchlistController(service);

        var response = controller.remove(21L);

        verify(service).remove(21L);
        assertEquals(204, response.getStatusCode().value());
    }
}
