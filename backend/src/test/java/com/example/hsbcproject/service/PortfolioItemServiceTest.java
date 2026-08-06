package com.example.hsbcproject.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.domain.Transaction;
import com.example.hsbcproject.domain.TransactionType;
import com.example.hsbcproject.dto.PortfolioSummaryResponse;
import com.example.hsbcproject.dto.TransactionResponse;
import com.example.hsbcproject.repository.PortfolioItemRepository;
import com.example.hsbcproject.repository.TransactionRepository;
import com.example.hsbcproject.support.TestDataFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class PortfolioItemServiceTest {
    @Mock private PortfolioItemRepository portfolioItemRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private DummyMarketDataStore dummyMarketDataStore;
    @InjectMocks private PortfolioItemService service;
    @Test
    void createUppercasesTickerAndLogsBuyTransaction() {
        PortfolioItem saved = TestDataFactory.portfolioItem();
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(saved);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(99L);
            return tx;
        });
        var response = service.create(TestDataFactory.createPortfolioItemRequest());
        assertEquals("AAPL", response.ticker());
        ArgumentCaptor<PortfolioItem> itemCaptor = ArgumentCaptor.forClass(PortfolioItem.class);
        verify(portfolioItemRepository).save(itemCaptor.capture());
        assertEquals("AAPL", itemCaptor.getValue().getTicker());
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertEquals(TransactionType.BUY, txCaptor.getValue().getTransactionType());
    }
    @Test
    void sellPartialQuantityUpdatesHoldingAndReturnsTransactionResponse() {
        PortfolioItem item = TestDataFactory.portfolioItem();
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(10L);
            return tx;
        });
        TransactionResponse response = service.sell(1L, new BigDecimal("150.00"), new BigDecimal("4.00000000"));
        assertEquals(new BigDecimal("600.0000000000"), response.totalValue());
        assertEquals(LocalDate.now(), response.transactionDate());
        verify(portfolioItemRepository).save(any(PortfolioItem.class));
        verify(portfolioItemRepository, never()).delete(any());
    }
    @Test
    void sellMoreThanAvailableThrowsException() {
        PortfolioItem item = TestDataFactory.portfolioItem();
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(item));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.sell(1L, new BigDecimal("150.00"), new BigDecimal("50.00000000")));
        assertTrue(ex.getMessage().contains("Cannot sell more than the current holding quantity"));
    }
    @Test
    void getSummaryAggregatesPortfolioTotals() {
        when(portfolioItemRepository.findAll()).thenReturn(List.of(TestDataFactory.portfolioItem(), TestDataFactory.secondPortfolioItem()));
        PortfolioSummaryResponse summary = service.getSummary();
        assertEquals(2, summary.totalPositions());
        assertEquals(new BigDecimal("15.00000000"), summary.totalQuantity());
        assertEquals(new BigDecimal("2000.0000000000"), summary.totalCostBasis());
        assertEquals(new BigDecimal("15.00000000"), summary.quantityByAssetType().get("STOCK"));
    }
}
