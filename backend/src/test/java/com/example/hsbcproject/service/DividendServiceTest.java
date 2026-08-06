package com.example.hsbcproject.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.domain.DividendRecord;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.DividendRepository;
import com.example.hsbcproject.support.TestDataFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class DividendServiceTest {
    @Mock private DividendRepository dividendRepository;
    @InjectMocks private DividendService service;
    @Test
    void createComputesTotalDividendAndUppercasesTicker() {
        when(dividendRepository.save(any(DividendRecord.class))).thenAnswer(invocation -> {
            DividendRecord record = invocation.getArgument(0);
            record.setId(5L);
            return record;
        });
        var response = service.create(TestDataFactory.createDividendRequest());
        assertEquals("MSFT", response.ticker());
        assertEquals(new BigDecimal("15.00"), response.totalDividend());
        ArgumentCaptor<DividendRecord> captor = ArgumentCaptor.forClass(DividendRecord.class);
        verify(dividendRepository).save(captor.capture());
        assertEquals("MSFT", captor.getValue().getTicker());
    }
    @Test
    void getTotalDividendsReceivedSumsAllRecords() {
        DividendRecord first = TestDataFactory.dividendRecord();
        DividendRecord second = TestDataFactory.dividendRecord();
        second.setId(2L);
        second.setTotalDividend(new BigDecimal("7.50"));
        when(dividendRepository.findAll()).thenReturn(List.of(first, second));
        assertEquals(new BigDecimal("12.50"), service.getTotalDividendsReceived());
    }
    @Test
    void deleteMissingDividendThrowsNotFound() {
        when(dividendRepository.findById(11L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(11L));
    }
}
