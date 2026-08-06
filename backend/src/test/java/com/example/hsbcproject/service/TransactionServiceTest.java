package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.Transaction;
import com.example.hsbcproject.domain.TransactionType;
import com.example.hsbcproject.dto.CreateTransactionRequest;
import com.example.hsbcproject.dto.TransactionResponse;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.TransactionRepository;
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
@DisplayName("TransactionService Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService service;

    private Transaction testTransaction;
    private CreateTransactionRequest createRequest;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setTicker("AAPL");
        testTransaction.setAssetType(AssetType.STOCK);
        testTransaction.setTransactionType(TransactionType.BUY);
        testTransaction.setQuantity(10);
        testTransaction.setPricePerUnit(new BigDecimal("100.00"));
        testTransaction.setTransactionDate(LocalDate.now());
        testTransaction.setNotes("Initial purchase");

        createRequest = new CreateTransactionRequest(
                "Aapl",
                AssetType.STOCK,
                TransactionType.BUY,
                10,
                new BigDecimal("100.00"),
                LocalDate.now(),
                "Initial purchase"
        );
    }

    @Test
    @DisplayName("findAll_withTransactions_returnsTransactionResponses")
    void testFindAll_WithTransactions_ReturnsTransactionResponses() {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<TransactionResponse> result = service.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).ticker());
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll_emptyRepository_returnsEmptyList")
    void testFindAll_EmptyRepository_ReturnsEmptyList() {
        when(transactionRepository.findAll()).thenReturn(new ArrayList<>());

        List<TransactionResponse> result = service.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById_validId_returnsTransactionResponse")
    void testFindById_ValidId_ReturnsTransactionResponse() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        TransactionResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals("AAPL", result.ticker());
        assertEquals(TransactionType.BUY, result.transactionType());
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById_invalidId_throwsResourceNotFoundException")
    void testFindById_InvalidId_ThrowsResourceNotFoundException() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(999L)
        );

        assertTrue(exception.getMessage().contains("999"));
        verify(transactionRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("findByTicker_validTicker_returnsMatchingTransactions")
    void testFindByTicker_ValidTicker_ReturnsMatchingTransactions() {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByTickerIgnoreCase("AAPL")).thenReturn(transactions);

        List<TransactionResponse> result = service.findByTicker("AAPL");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).ticker());
        verify(transactionRepository, times(1)).findByTickerIgnoreCase("AAPL");
    }

    @Test
    @DisplayName("findByTicker_tickerNotFound_returnsEmptyList")
    void testFindByTicker_TickerNotFound_ReturnsEmptyList() {
        when(transactionRepository.findByTickerIgnoreCase("UNKNOWN")).thenReturn(new ArrayList<>());

        List<TransactionResponse> result = service.findByTicker("UNKNOWN");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findByTickerIgnoreCase("UNKNOWN");
    }

    @Test
    @DisplayName("findByTicker_caseInsensitive_findsTransactions")
    void testFindByTicker_CaseInsensitive_FindsTransactions() {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByTickerIgnoreCase("aapl")).thenReturn(transactions);

        List<TransactionResponse> result = service.findByTicker("aapl");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findByTickerIgnoreCase("aapl");
    }

    @Test
    @DisplayName("create_validRequest_savesAndConvertsToUpperCase")
    void testCreate_ValidRequest_SavesAndConvertsToUpperCase() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionResponse result = service.create(createRequest);

        assertNotNull(result);
        assertEquals("AAPL", result.ticker());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());
        Transaction savedTx = captor.getValue();
        assertEquals("AAPL", savedTx.getTicker());
        assertEquals(TransactionType.BUY, savedTx.getTransactionType());
        assertEquals(10, savedTx.getQuantity());
    }

    @Test
    @DisplayName("create_validRequest_calculatesCorrectTotal")
    void testCreate_ValidRequest_CalculatesCorrectTotal() {
        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setTicker("AAPL");
        tx.setAssetType(AssetType.STOCK);
        tx.setTransactionType(TransactionType.BUY);
        tx.setQuantity(10);
        tx.setPricePerUnit(new BigDecimal("100.00"));
        tx.setTransactionDate(LocalDate.now());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(tx);

        TransactionResponse result = service.create(createRequest);

        assertNotNull(result.totalValue());
        assertEquals(new BigDecimal("1000.00"), result.totalValue());
    }

    @Test
    @DisplayName("create_sellTransaction_savesCorrectly")
    void testCreate_SellTransaction_SavesCorrectly() {
        CreateTransactionRequest sellRequest = new CreateTransactionRequest(
                "MSFT",
                AssetType.STOCK,
                TransactionType.SELL,
                5,
                new BigDecimal("150.00"),
                LocalDate.now(),
                "Partial sale"
        );

        Transaction sellTx = new Transaction();
        sellTx.setId(2L);
        sellTx.setTicker("MSFT");
        sellTx.setAssetType(AssetType.STOCK);
        sellTx.setTransactionType(TransactionType.SELL);
        sellTx.setQuantity(5);
        sellTx.setPricePerUnit(new BigDecimal("150.00"));
        sellTx.setTransactionDate(LocalDate.now());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(sellTx);

        TransactionResponse result = service.create(sellRequest);

        assertEquals(TransactionType.SELL, result.transactionType());
        assertEquals(5, result.quantity());
    }

    @Test
    @DisplayName("create_withNullNotes_savesSuccessfully")
    void testCreate_WithNullNotes_SavesSuccessfully() {
        CreateTransactionRequest requestWithoutNotes = new CreateTransactionRequest(
                "AAPL",
                AssetType.STOCK,
                TransactionType.BUY,
                10,
                new BigDecimal("100.00"),
                LocalDate.now(),
                null
        );

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setTicker("AAPL");
        tx.setAssetType(AssetType.STOCK);
        tx.setTransactionType(TransactionType.BUY);
        tx.setQuantity(10);
        tx.setPricePerUnit(new BigDecimal("100.00"));
        tx.setTransactionDate(LocalDate.now());
        tx.setNotes(null);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(tx);

        TransactionResponse result = service.create(requestWithoutNotes);

        assertNotNull(result);
        assertNull(result.notes());
    }

    @Test
    @DisplayName("delete_validId_deletesTransaction")
    void testDelete_ValidId_DeletesTransaction() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        service.delete(1L);

        verify(transactionRepository, times(1)).delete(testTransaction);
    }

    @Test
    @DisplayName("delete_invalidId_throwsResourceNotFoundException")
    void testDelete_InvalidId_ThrowsResourceNotFoundException() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(999L)
        );

        verify(transactionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("findByTicker_multipleTransactions_returnsAll")
    void testFindByTicker_MultipleTransactions_ReturnsAll() {
        Transaction tx1 = new Transaction();
        tx1.setId(1L);
        tx1.setTicker("AAPL");
        tx1.setTransactionType(TransactionType.BUY);
        tx1.setQuantity(10);
        tx1.setPricePerUnit(new BigDecimal("100.00"));

        Transaction tx2 = new Transaction();
        tx2.setId(2L);
        tx2.setTicker("AAPL");
        tx2.setTransactionType(TransactionType.SELL);
        tx2.setQuantity(5);
        tx2.setPricePerUnit(new BigDecimal("110.00"));

        when(transactionRepository.findByTickerIgnoreCase("AAPL")).thenReturn(Arrays.asList(tx1, tx2));

        List<TransactionResponse> result = service.findByTicker("AAPL");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.transactionType() == TransactionType.BUY));
        assertTrue(result.stream().anyMatch(r -> r.transactionType() == TransactionType.SELL));
    }

    @Test
    @DisplayName("create_largeQuantity_savesCorrectly")
    void testCreate_LargeQuantity_SavesCorrectly() {
        CreateTransactionRequest largeRequest = new CreateTransactionRequest(
                "AAPL",
                AssetType.STOCK,
                TransactionType.BUY,
                10000,
                new BigDecimal("100.00"),
                LocalDate.now(),
                "Large purchase"
        );

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setTicker("AAPL");
        tx.setQuantity(10000);
        tx.setPricePerUnit(new BigDecimal("100.00"));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(tx);

        TransactionResponse result = service.create(largeRequest);

        assertEquals(10000, result.quantity());
        assertEquals(new BigDecimal("1000000.00"), result.totalValue());
    }

    @Test
    @DisplayName("create_smallPrice_calculatesCorrectTotal")
    void testCreate_SmallPrice_CalculatesCorrectTotal() {
        CreateTransactionRequest smallPriceRequest = new CreateTransactionRequest(
                "AAPL",
                AssetType.STOCK,
                TransactionType.BUY,
                100,
                new BigDecimal("0.01"),
                LocalDate.now(),
                "Small price purchase"
        );

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setTicker("AAPL");
        tx.setQuantity(100);
        tx.setPricePerUnit(new BigDecimal("0.01"));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(tx);

        TransactionResponse result = service.create(smallPriceRequest);

        assertEquals(new BigDecimal("1.00"), result.totalValue());
    }
}



