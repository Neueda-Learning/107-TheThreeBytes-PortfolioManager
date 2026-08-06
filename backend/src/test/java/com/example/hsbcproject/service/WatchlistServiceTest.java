package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.WatchlistItem;
import com.example.hsbcproject.dto.CreateWatchlistItemRequest;
import com.example.hsbcproject.dto.WatchlistItemResponse;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.WatchlistItemRepository;
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
@DisplayName("WatchlistService Tests")
class WatchlistServiceTest {

    @Mock
    private WatchlistItemRepository watchlistItemRepository;

    @InjectMocks
    private WatchlistService service;

    private WatchlistItem testItem;
    private CreateWatchlistItemRequest createRequest;

    @BeforeEach
    void setUp() {
        testItem = new WatchlistItem();
        testItem.setId(1L);
        testItem.setTicker("AAPL");
        testItem.setAssetType(AssetType.STOCK);
        testItem.setAddedDate(LocalDate.now());

        createRequest = new CreateWatchlistItemRequest("Aapl", AssetType.STOCK);
    }

    @Test
    @DisplayName("findAll_withItems_returnsWatchlistItemResponses")
    void testFindAll_WithItems_ReturnsWatchlistItemResponses() {
        List<WatchlistItem> items = Arrays.asList(testItem);
        when(watchlistItemRepository.findAll()).thenReturn(items);

        List<WatchlistItemResponse> result = service.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).ticker());
        assertEquals(AssetType.STOCK, result.get(0).assetType());
        verify(watchlistItemRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll_emptyRepository_returnsEmptyList")
    void testFindAll_EmptyRepository_ReturnsEmptyList() {
        when(watchlistItemRepository.findAll()).thenReturn(new ArrayList<>());

        List<WatchlistItemResponse> result = service.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(watchlistItemRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("add_validRequest_createsAndReturnsWatchlistItem")
    void testAdd_ValidRequest_CreatesAndReturnsWatchlistItem() {
        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenReturn(testItem);

        WatchlistItemResponse result = service.add(createRequest);

        assertNotNull(result);
        assertEquals("AAPL", result.ticker());
        assertEquals(AssetType.STOCK, result.assetType());
        assertNotNull(result.addedDate());

        ArgumentCaptor<WatchlistItem> captor = ArgumentCaptor.forClass(WatchlistItem.class);
        verify(watchlistItemRepository, times(1)).save(captor.capture());
        WatchlistItem saved = captor.getValue();
        assertEquals("AAPL", saved.getTicker());
        assertEquals(LocalDate.now(), saved.getAddedDate());
    }

    @Test
    @DisplayName("add_lowerCaseTicker_convertsToUpperCase")
    void testAdd_LowerCaseTicker_ConvertsToUpperCase() {
        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenReturn(testItem);

        service.add(createRequest);

        ArgumentCaptor<WatchlistItem> captor = ArgumentCaptor.forClass(WatchlistItem.class);
        verify(watchlistItemRepository, times(1)).save(captor.capture());
        assertEquals("AAPL", captor.getValue().getTicker());
    }

    @Test
    @DisplayName("add_todaysDateSet_returnsTodaysDate")
    void testAdd_TodaysDateSet_ReturnsTodaysDate() {
        LocalDate today = LocalDate.now();
        testItem.setAddedDate(today);
        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenReturn(testItem);

        WatchlistItemResponse result = service.add(createRequest);

        assertEquals(today, result.addedDate());
    }

    @Test
    @DisplayName("add_bondAsset_createsCorrectly")
    void testAdd_BondAsset_CreatesCorrectly() {
        CreateWatchlistItemRequest bondRequest = new CreateWatchlistItemRequest("BOND1", AssetType.BOND);

        WatchlistItem bondItem = new WatchlistItem();
        bondItem.setId(2L);
        bondItem.setTicker("BOND1");
        bondItem.setAssetType(AssetType.BOND);
        bondItem.setAddedDate(LocalDate.now());

        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenReturn(bondItem);

        WatchlistItemResponse result = service.add(bondRequest);

        assertEquals("BOND1", result.ticker());
        assertEquals(AssetType.BOND, result.assetType());
    }

    @Test
    @DisplayName("add_cryptoAsset_createsCorrectly")
    void testAdd_CryptoAsset_CreatesCorrectly() {
        CreateWatchlistItemRequest cryptoRequest = new CreateWatchlistItemRequest("BTC", AssetType.CRYPTO);

        WatchlistItem cryptoItem = new WatchlistItem();
        cryptoItem.setId(3L);
        cryptoItem.setTicker("BTC");
        cryptoItem.setAssetType(AssetType.CRYPTO);
        cryptoItem.setAddedDate(LocalDate.now());

        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenReturn(cryptoItem);

        WatchlistItemResponse result = service.add(cryptoRequest);

        assertEquals("BTC", result.ticker());
        assertEquals(AssetType.CRYPTO, result.assetType());
    }

    @Test
    @DisplayName("remove_validId_removesItem")
    void testRemove_ValidId_RemovesItem() {
        when(watchlistItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        service.remove(1L);

        verify(watchlistItemRepository, times(1)).delete(testItem);
    }

    @Test
    @DisplayName("remove_invalidId_throwsResourceNotFoundException")
    void testRemove_InvalidId_ThrowsResourceNotFoundException() {
        when(watchlistItemRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.remove(999L)
        );

        assertTrue(exception.getMessage().contains("999"));
        verify(watchlistItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("isWatched_tickerWatched_returnsTrue")
    void testIsWatched_TickerWatched_ReturnsTrue() {
        when(watchlistItemRepository.existsByTickerIgnoreCase("AAPL")).thenReturn(true);

        boolean result = service.isWatched("AAPL");

        assertTrue(result);
        verify(watchlistItemRepository, times(1)).existsByTickerIgnoreCase("AAPL");
    }

    @Test
    @DisplayName("isWatched_tickerNotWatched_returnsFalse")
    void testIsWatched_TickerNotWatched_ReturnsFalse() {
        when(watchlistItemRepository.existsByTickerIgnoreCase("UNKNOWN")).thenReturn(false);

        boolean result = service.isWatched("UNKNOWN");

        assertFalse(result);
        verify(watchlistItemRepository, times(1)).existsByTickerIgnoreCase("UNKNOWN");
    }

    @Test
    @DisplayName("isWatched_caseInsensitive_findsWatchedTicker")
    void testIsWatched_CaseInsensitive_FindsWatchedTicker() {
        when(watchlistItemRepository.existsByTickerIgnoreCase("aapl")).thenReturn(true);

        boolean result = service.isWatched("aapl");

        assertTrue(result);
        verify(watchlistItemRepository, times(1)).existsByTickerIgnoreCase("aapl");
    }

    @Test
    @DisplayName("isWatched_mixedCase_findsWatchedTicker")
    void testIsWatched_MixedCase_FindsWatchedTicker() {
        when(watchlistItemRepository.existsByTickerIgnoreCase("AaPl")).thenReturn(true);

        boolean result = service.isWatched("AaPl");

        assertTrue(result);
        verify(watchlistItemRepository, times(1)).existsByTickerIgnoreCase("AaPl");
    }

    @Test
    @DisplayName("findAll_multipleItems_returnsAllItems")
    void testFindAll_MultipleItems_ReturnsAllItems() {
        WatchlistItem item1 = new WatchlistItem();
        item1.setId(1L);
        item1.setTicker("AAPL");
        item1.setAssetType(AssetType.STOCK);
        item1.setAddedDate(LocalDate.now());

        WatchlistItem item2 = new WatchlistItem();
        item2.setId(2L);
        item2.setTicker("BOND1");
        item2.setAssetType(AssetType.BOND);
        item2.setAddedDate(LocalDate.now());

        WatchlistItem item3 = new WatchlistItem();
        item3.setId(3L);
        item3.setTicker("BTC");
        item3.setAssetType(AssetType.CRYPTO);
        item3.setAddedDate(LocalDate.now());

        when(watchlistItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2, item3));

        List<WatchlistItemResponse> result = service.findAll();

        assertEquals(3, result.size());
        assertEquals("AAPL", result.get(0).ticker());
        assertEquals("BOND1", result.get(1).ticker());
        assertEquals("BTC", result.get(2).ticker());
    }

    @Test
    @DisplayName("add_multipleItemsSameTicker_createsMultipleRecords")
    void testAdd_MultipleItemsSameTicker_CreatesMultipleRecords() {
        WatchlistItem item1 = new WatchlistItem();
        item1.setId(1L);
        item1.setTicker("AAPL");
        item1.setAssetType(AssetType.STOCK);
        item1.setAddedDate(LocalDate.now());

        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenReturn(item1);

        WatchlistItemResponse result = service.add(createRequest);

        assertNotNull(result);
        verify(watchlistItemRepository, times(1)).save(any(WatchlistItem.class));
    }

    @Test
    @DisplayName("remove_multipleRemoves_worksCorrectly")
    void testRemove_MultipleRemoves_WorksCorrectly() {
        WatchlistItem item1 = new WatchlistItem();
        item1.setId(1L);

        WatchlistItem item2 = new WatchlistItem();
        item2.setId(2L);

        when(watchlistItemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(watchlistItemRepository.findById(2L)).thenReturn(Optional.of(item2));

        service.remove(1L);
        service.remove(2L);

        verify(watchlistItemRepository, times(1)).delete(item1);
        verify(watchlistItemRepository, times(1)).delete(item2);
    }

    @Test
    @DisplayName("isWatched_afterRemoval_returnsFalse")
    void testIsWatched_InteractionWithAdd_ReturnsTrue() {
        when(watchlistItemRepository.save(any(WatchlistItem.class))).thenReturn(testItem);
        when(watchlistItemRepository.existsByTickerIgnoreCase("AAPL")).thenReturn(true);

        service.add(createRequest);
        boolean result = service.isWatched("AAPL");

        assertTrue(result);
    }
}

