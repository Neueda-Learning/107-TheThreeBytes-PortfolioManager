package com.example.hsbcproject.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.domain.Transaction;
import com.example.hsbcproject.dto.TransactionResponse;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.TransactionRepository;
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
class TransactionServiceTest {
    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private TransactionService service;
    @Test
    void createUppercasesTickerAndCalculatesResponseTotal() {
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(7L);
            return tx;
        });
        TransactionResponse response = service.create(TestDataFactory.createTransactionRequest());
        assertEquals("MSFT", response.ticker());
        assertEquals(new BigDecimal("550.0000000000"), response.totalValue());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertEquals("MSFT", captor.getValue().getTicker());
    }
    @Test
    void findByTickerReturnsMappedResponses() {
        when(transactionRepository.findByTickerIgnoreCase("aapl")).thenReturn(List.of(TestDataFactory.transaction()));
        List<TransactionResponse> result = service.findByTicker("aapl");
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).ticker());
    }
    @Test
    void deleteMissingTransactionThrowsNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }
}
