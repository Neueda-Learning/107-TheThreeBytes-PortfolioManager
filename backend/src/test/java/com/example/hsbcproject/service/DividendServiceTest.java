package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.domain.DividendRecord;
import com.example.hsbcproject.dto.CreateDividendRequest;
import com.example.hsbcproject.dto.DividendResponse;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.DividendRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DividendService Tests")
class DividendServiceTest {

    @Mock
    private DividendRepository dividendRepository;

    @InjectMocks
    private DividendService service;

    private DividendRecord testDividend;
    private CreateDividendRequest createRequest;

    @BeforeEach
    void setUp() {
        testDividend = new DividendRecord();
        testDividend.setId(1L);
        testDividend.setTicker("AAPL");
        testDividend.setDividendPerShare(new BigDecimal("0.23"));
        testDividend.setSharesHeld(100);
        testDividend.setTotalDividend(new BigDecimal("23.00"));
        testDividend.setDividendDate(LocalDate.now());

        createRequest = new CreateDividendRequest(
                "Aapl",
                new BigDecimal("0.23"),
                100,
                LocalDate.now()
        );
    }

    @Test
    @DisplayName("findAll_withDividends_returnsDividendResponses")
    void testFindAll_WithDividends_ReturnsDividendResponses() {
        List<DividendRecord> records = Arrays.asList(testDividend);
        when(dividendRepository.findAll()).thenReturn(records);

        List<DividendResponse> result = service.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).ticker());
        assertEquals(new BigDecimal("0.23"), result.get(0).dividendPerShare());
        verify(dividendRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll_emptyRepository_returnsEmptyList")
    void testFindAll_EmptyRepository_ReturnsEmptyList() {
        when(dividendRepository.findAll()).thenReturn(new ArrayList<>());

        List<DividendResponse> result = service.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dividendRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById_validId_returnsDividendResponse")
    void testFindById_ValidId_ReturnsDividendResponse() {
        when(dividendRepository.findById(1L)).thenReturn(Optional.of(testDividend));

        DividendResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals("AAPL", result.ticker());
        assertEquals(100, result.sharesHeld());
        verify(dividendRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById_invalidId_throwsResourceNotFoundException")
    void testFindById_InvalidId_ThrowsResourceNotFoundException() {
        when(dividendRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(999L)
        );

        assertTrue(exception.getMessage().contains("999"));
        verify(dividendRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("findByTicker_validTicker_returnsMatchingRecords")
    void testFindByTicker_ValidTicker_ReturnsMatchingRecords() {
        List<DividendRecord> records = Arrays.asList(testDividend);
        when(dividendRepository.findByTickerIgnoreCase("AAPL")).thenReturn(records);

        List<DividendResponse> result = service.findByTicker("AAPL");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).ticker());
        verify(dividendRepository, times(1)).findByTickerIgnoreCase("AAPL");
    }

    @Test
    @DisplayName("findByTicker_tickerNotFound_returnsEmptyList")
    void testFindByTicker_TickerNotFound_ReturnsEmptyList() {
        when(dividendRepository.findByTickerIgnoreCase("UNKNOWN")).thenReturn(new ArrayList<>());

        List<DividendResponse> result = service.findByTicker("UNKNOWN");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dividendRepository, times(1)).findByTickerIgnoreCase("UNKNOWN");
    }

    @Test
    @DisplayName("findByTicker_caseInsensitive_findsRecords")
    void testFindByTicker_CaseInsensitive_FindsRecords() {
        List<DividendRecord> records = Arrays.asList(testDividend);
        when(dividendRepository.findByTickerIgnoreCase("aapl")).thenReturn(records);

        List<DividendResponse> result = service.findByTicker("aapl");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(dividendRepository, times(1)).findByTickerIgnoreCase("aapl");
    }

    @Test
    @DisplayName("create_validRequest_calculatesAndSavesDividend")
    void testCreate_ValidRequest_CalculatesAndSavesDividend() {
        when(dividendRepository.save(any(DividendRecord.class))).thenReturn(testDividend);

        DividendResponse result = service.create(createRequest);

        assertNotNull(result);
        assertEquals("AAPL", result.ticker());
        assertEquals(new BigDecimal("23.00"), result.totalDividend());

        ArgumentCaptor<DividendRecord> captor = ArgumentCaptor.forClass(DividendRecord.class);
        verify(dividendRepository, times(1)).save(captor.capture());
        DividendRecord saved = captor.getValue();
        assertEquals("AAPL", saved.getTicker());
        assertEquals(new BigDecimal("23.00"), saved.getTotalDividend());
    }

    @Test
    @DisplayName("create_lowerCaseTicker_convertsToUpperCase")
    void testCreate_LowerCaseTicker_ConvertsToUpperCase() {
        when(dividendRepository.save(any(DividendRecord.class))).thenReturn(testDividend);

        service.create(createRequest);

        ArgumentCaptor<DividendRecord> captor = ArgumentCaptor.forClass(DividendRecord.class);
        verify(dividendRepository, times(1)).save(captor.capture());
        assertEquals("AAPL", captor.getValue().getTicker());
    }

    @Test
    @DisplayName("create_totalDividendCalculation_isCorrect")
    void testCreate_TotalDividendCalculation_IsCorrect() {
        DividendRecord saved = new DividendRecord();
        saved.setId(1L);
        saved.setTicker("MSFT");
        saved.setDividendPerShare(new BigDecimal("0.68"));
        saved.setSharesHeld(50);
        saved.setTotalDividend(new BigDecimal("34.00"));
        saved.setDividendDate(LocalDate.now());

        when(dividendRepository.save(any(DividendRecord.class))).thenReturn(saved);

        CreateDividendRequest request = new CreateDividendRequest(
                "MSFT",
                new BigDecimal("0.68"),
                50,
                LocalDate.now()
        );

        DividendResponse result = service.create(request);

        assertEquals(new BigDecimal("34.00"), result.totalDividend());
    }

    @Test
    @DisplayName("delete_validId_deletesDividend")
    void testDelete_ValidId_DeletesDividend() {
        when(dividendRepository.findById(1L)).thenReturn(Optional.of(testDividend));

        service.delete(1L);

        verify(dividendRepository, times(1)).delete(testDividend);
    }

    @Test
    @DisplayName("delete_invalidId_throwsResourceNotFoundException")
    void testDelete_InvalidId_ThrowsResourceNotFoundException() {
        when(dividendRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(999L)
        );

        verify(dividendRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getTotalDividendsReceived_withDividends_returnsSumOfAll")
    void testGetTotalDividendsReceived_WithDividends_ReturnsSumOfAll() {
        DividendRecord div1 = new DividendRecord();
        div1.setTotalDividend(new BigDecimal("23.00"));

        DividendRecord div2 = new DividendRecord();
        div2.setTotalDividend(new BigDecimal("34.00"));

        DividendRecord div3 = new DividendRecord();
        div3.setTotalDividend(new BigDecimal("12.50"));

        when(dividendRepository.findAll()).thenReturn(Arrays.asList(div1, div2, div3));

        BigDecimal result = service.getTotalDividendsReceived();

        assertEquals(new BigDecimal("69.50"), result);
        verify(dividendRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getTotalDividendsReceived_emptyRepository_returnsZero")
    void testGetTotalDividendsReceived_EmptyRepository_ReturnsZero() {
        when(dividendRepository.findAll()).thenReturn(new ArrayList<>());

        BigDecimal result = service.getTotalDividendsReceived();

        assertEquals(BigDecimal.ZERO, result);
        verify(dividendRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getTotalDividendsReceived_singleDividend_returnsValue")
    void testGetTotalDividendsReceived_SingleDividend_ReturnsValue() {
        DividendRecord div = new DividendRecord();
        div.setTotalDividend(new BigDecimal("23.00"));

        when(dividendRepository.findAll()).thenReturn(Arrays.asList(div));

        BigDecimal result = service.getTotalDividendsReceived();

        assertEquals(new BigDecimal("23.00"), result);
    }

    @Test
    @DisplayName("getTotalDividendsReceived_largeDividends_calculatesCorrectly")
    void testGetTotalDividendsReceived_LargeDividends_CalculatesCorrectly() {
        DividendRecord div1 = new DividendRecord();
        div1.setTotalDividend(new BigDecimal("10000.00"));

        DividendRecord div2 = new DividendRecord();
        div2.setTotalDividend(new BigDecimal("5000.50"));

        when(dividendRepository.findAll()).thenReturn(Arrays.asList(div1, div2));

        BigDecimal result = service.getTotalDividendsReceived();

        assertEquals(new BigDecimal("15000.50"), result);
    }

    @Test
    @DisplayName("create_zeroSharesHeld_savesCorrectly")
    void testCreate_ZeroSharesHeld_SavesCorrectly() {
        DividendRecord saved = new DividendRecord();
        saved.setId(1L);
        saved.setTicker("AAPL");
        saved.setDividendPerShare(new BigDecimal("0.23"));
        saved.setSharesHeld(0);
        saved.setTotalDividend(BigDecimal.ZERO);
        saved.setDividendDate(LocalDate.now());

        when(dividendRepository.save(any(DividendRecord.class))).thenReturn(saved);

        CreateDividendRequest request = new CreateDividendRequest(
                "AAPL",
                new BigDecimal("0.23"),
                0,
                LocalDate.now()
        );

        DividendResponse result = service.create(request);

        assertEquals(0, result.sharesHeld());
        assertEquals(BigDecimal.ZERO, result.totalDividend());
    }

    @Test
    @DisplayName("create_largeShareCount_calculatesCorrectly")
    void testCreate_LargeShareCount_CalculatesCorrectly() {
        DividendRecord saved = new DividendRecord();
        saved.setId(1L);
        saved.setTicker("AAPL");
        saved.setDividendPerShare(new BigDecimal("0.23"));
        saved.setSharesHeld(100000);
        saved.setTotalDividend(new BigDecimal("23000.00"));
        saved.setDividendDate(LocalDate.now());

        when(dividendRepository.save(any(DividendRecord.class))).thenReturn(saved);

        CreateDividendRequest request = new CreateDividendRequest(
                "AAPL",
                new BigDecimal("0.23"),
                100000,
                LocalDate.now()
        );

        DividendResponse result = service.create(request);

        assertEquals(new BigDecimal("23000.00"), result.totalDividend());
    }
}

