package com.example.hsbcproject.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.service.TaxService;
import org.junit.jupiter.api.Test;
class TaxControllerTest {
    @Test
    void estimateTaxDelegatesToService() {
        TaxService service = mock(TaxService.class);
        when(service.estimateTax()).thenReturn(java.util.List.of());
        TaxController controller = new TaxController(service);
        assertEquals(0, controller.estimateTax().size());
        verify(service).estimateTax();
    }
}
