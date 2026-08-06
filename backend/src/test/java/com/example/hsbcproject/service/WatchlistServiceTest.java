package com.example.hsbcproject.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.hsbcproject.domain.WatchlistItem;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.WatchlistItemRepository;
import com.example.hsbcproject.support.TestDataFactory;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {
    @Mock private WatchlistItemRepository watchlistItemRepository;
    @InjectMocks private WatchlistService service;
    @Test
    void addUppercasesTickerAndSetsAddedDate() {
        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenAnswer(invocation -> {
            WatchlistItem item = invocation.getArgument(0);
            item.setId(3L);
            return item;
        });
        var response = service.add(TestDataFactory.createWatchlistItemRequest());
        assertEquals("ETH", response.ticker());
        assertEquals(LocalDate.now(), response.addedDate());
        ArgumentCaptor<WatchlistItem> captor = ArgumentCaptor.forClass(WatchlistItem.class);
        verify(watchlistItemRepository).save(captor.capture());
        assertEquals("ETH", captor.getValue().getTicker());
    }
    @Test
    void isWatchedDelegatesToRepository() {
        when(watchlistItemRepository.existsByTickerIgnoreCase("eth")).thenReturn(true);
        assertTrue(service.isWatched("eth"));
    }
    @Test
    void removeMissingWatchlistItemThrowsNotFound() {
        when(watchlistItemRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.remove(42L));
    }
}
