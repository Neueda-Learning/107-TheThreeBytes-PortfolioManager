package com.example.hsbcproject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.domain.Transaction;
import com.example.hsbcproject.domain.TransactionType;
import com.example.hsbcproject.dto.CreatePortfolioItemRequest;
import com.example.hsbcproject.dto.PortfolioItemResponse;
import com.example.hsbcproject.dto.PortfolioSummaryResponse;
import com.example.hsbcproject.dto.TransactionResponse;
import com.example.hsbcproject.dto.UpdatePortfolioItemRequest;
import com.example.hsbcproject.exception.ResourceNotFoundException;
import com.example.hsbcproject.repository.PortfolioItemRepository;
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
@DisplayName("PortfolioItemService Tests")
class PortfolioItemServiceTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PortfolioItemService service;

    private PortfolioItem testItem;
    private CreatePortfolioItemRequest createRequest;
    private UpdatePortfolioItemRequest updateRequest;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testItem = new PortfolioItem();
        testItem.setId(1L);
        testItem.setTicker("AAPL");
        testItem.setQuantity(10);
        testItem.setAssetType(AssetType.STOCK);
        testItem.setPurchasePrice(new BigDecimal("100.00"));
        testItem.setPurchaseDate(LocalDate.now().minusMonths(6));
        testItem.setName("Apple Inc.");
        testItem.setSector("Technology");

        createRequest = new CreatePortfolioItemRequest(
                "Aapl",
                10,
                AssetType.STOCK,
                new BigDecimal("100.00"),
                LocalDate.now().minusMonths(6),
                "Apple Inc.",
                "Technology",
                null,
                null,
                null
        );

        updateRequest = new UpdatePortfolioItemRequest(
                "MSFT",
                15,
                AssetType.STOCK,
                new BigDecimal("150.00"),
                LocalDate.now().minusMonths(3),
                "Microsoft",
                "Technology",
                null,
                null,
                null
        );

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setTicker("AAPL");
        testTransaction.setAssetType(AssetType.STOCK);
        testTransaction.setTransactionType(TransactionType.BUY);
        testTransaction.setQuantity(10);
        testTransaction.setPricePerUnit(new BigDecimal("100.00"));
        testTransaction.setTransactionDate(LocalDate.now());
    }

    @Test
    @DisplayName("findAll_withItems_returnsItemResponses")
    void testFindAll_WithItems_ReturnsItemResponses() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);

        List<PortfolioItemResponse> result = service.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).ticker());
        assertEquals(10, result.get(0).quantity());
        verify(portfolioItemRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll_emptyRepository_returnsEmptyList")
    void testFindAll_EmptyRepository_ReturnsEmptyList() {
        when(portfolioItemRepository.findAll()).thenReturn(new ArrayList<>());

        List<PortfolioItemResponse> result = service.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(portfolioItemRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById_validId_returnsItemResponse")
    void testFindById_ValidId_ReturnsItemResponse() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        PortfolioItemResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals("AAPL", result.ticker());
        assertEquals(10, result.quantity());
        verify(portfolioItemRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById_invalidId_throwsResourceNotFoundException")
    void testFindById_InvalidId_ThrowsResourceNotFoundException() {
        when(portfolioItemRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(999L)
        );

        assertTrue(exception.getMessage().contains("999"));
        verify(portfolioItemRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("create_validRequest_savesAndLogsTransaction")
    void testCreate_ValidRequest_SavesAndLogsTransaction() {
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(testItem);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PortfolioItemResponse result = service.create(createRequest);

        assertNotNull(result);
        assertEquals("AAPL", result.ticker());

        ArgumentCaptor<PortfolioItem> itemCaptor = ArgumentCaptor.forClass(PortfolioItem.class);
        verify(portfolioItemRepository, times(1)).save(itemCaptor.capture());
        PortfolioItem savedItem = itemCaptor.getValue();
        assertEquals("AAPL", savedItem.getTicker()); // Verify uppercase conversion

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("create_invalidMaturityDate_throwsIllegalArgument")
    void testCreate_InvalidMaturityDate_ThrowsIllegalArgument() {
        LocalDate today = LocalDate.now();
        CreatePortfolioItemRequest invalidRequest = new CreatePortfolioItemRequest(
                "BOND1",
                10,
                AssetType.BOND,
                new BigDecimal("100.00"),
                today,
                "Bond",
                null,
                null,
                null,
                today.minusDays(1) // maturity date before purchase date
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(invalidRequest)
        );

        assertTrue(exception.getMessage().contains("maturityDate must be after purchaseDate"));
        verify(portfolioItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("create_lowerCaseTicker_convertsToUpperCase")
    void testCreate_LowerCaseTicker_ConvertsToUpperCase() {
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(testItem);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(createRequest);

        ArgumentCaptor<PortfolioItem> captor = ArgumentCaptor.forClass(PortfolioItem.class);
        verify(portfolioItemRepository, times(1)).save(captor.capture());
        assertEquals("AAPL", captor.getValue().getTicker());
    }

    @Test
    @DisplayName("update_validRequest_updatesAndReturnsResponse")
    void testUpdate_ValidRequest_UpdatesAndReturnsResponse() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(testItem);

        PortfolioItemResponse result = service.update(1L, updateRequest);

        assertNotNull(result);

        ArgumentCaptor<PortfolioItem> captor = ArgumentCaptor.forClass(PortfolioItem.class);
        verify(portfolioItemRepository, times(1)).save(captor.capture());
        PortfolioItem savedItem = captor.getValue();
        assertEquals("MSFT", savedItem.getTicker());
        assertEquals(15, savedItem.getQuantity());
    }

    @Test
    @DisplayName("update_invalidId_throwsResourceNotFoundException")
    void testUpdate_InvalidId_ThrowsResourceNotFoundException() {
        when(portfolioItemRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.update(999L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("999"));
        verify(portfolioItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("update_invalidMaturityDate_throwsIllegalArgument")
    void testUpdate_InvalidMaturityDate_ThrowsIllegalArgument() {
        LocalDate today = LocalDate.now();
        UpdatePortfolioItemRequest invalidRequest = new UpdatePortfolioItemRequest(
                "BOND1",
                10,
                AssetType.BOND,
                new BigDecimal("100.00"),
                today,
                "Bond",
                null,
                null,
                null,
                today.minusDays(1)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(1L, invalidRequest)
        );

        assertTrue(exception.getMessage().contains("maturityDate must be after purchaseDate"));
        verify(portfolioItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete_validId_deletesItem")
    void testDelete_ValidId_DeletesItem() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        service.delete(1L);

        verify(portfolioItemRepository, times(1)).delete(testItem);
    }

    @Test
    @DisplayName("delete_invalidId_throwsResourceNotFoundException")
    void testDelete_InvalidId_ThrowsResourceNotFoundException() {
        when(portfolioItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(999L)
        );

        verify(portfolioItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("sell_validholding_createsTransactionAndDeletesItem")
    void testSell_ValidHolding_CreatesTransactionAndDeletesItem() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal sellPrice = new BigDecimal("150.00");
        TransactionResponse result = service.sell(1L, sellPrice);

        assertNotNull(result);
        assertNotNull(result.totalValue());
        
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(portfolioItemRepository, times(1)).delete(testItem);
    }

    @Test
    @DisplayName("sell_invalidId_throwsResourceNotFoundException")
    void testSell_InvalidId_ThrowsResourceNotFoundException() {
        when(portfolioItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.sell(999L, new BigDecimal("100.00"))
        );

        verify(portfolioItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getSummary_withItems_returnsCorrectSummary")
    void testGetSummary_WithItems_ReturnsCorrectSummary() {
        List<PortfolioItem> items = Arrays.asList(testItem);
        when(portfolioItemRepository.findAll()).thenReturn(items);

        PortfolioSummaryResponse result = service.getSummary();

        assertNotNull(result);
        assertEquals(1, result.totalPositions());
        assertEquals(10, result.totalQuantity());
        assertEquals(new BigDecimal("1000.00"), result.totalCostBasis());
        verify(portfolioItemRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getSummary_emptyPortfolio_returnsZeroValues")
    void testGetSummary_EmptyPortfolio_ReturnsZeroValues() {
        when(portfolioItemRepository.findAll()).thenReturn(new ArrayList<>());

        PortfolioSummaryResponse result = service.getSummary();

        assertNotNull(result);
        assertEquals(0, result.totalPositions());
        assertEquals(0, result.totalQuantity());
        assertEquals(BigDecimal.ZERO, result.totalCostBasis());
    }

    @Test
    @DisplayName("getSummary_multipleAssetTypes_groupsByType")
    void testGetSummary_MultipleAssetTypes_GroupsByType() {
        PortfolioItem stock = new PortfolioItem();
        stock.setId(1L);
        stock.setTicker("AAPL");
        stock.setQuantity(10);
        stock.setAssetType(AssetType.STOCK);
        stock.setPurchasePrice(new BigDecimal("100.00"));
        stock.setPurchaseDate(LocalDate.now());

        PortfolioItem bond = new PortfolioItem();
        bond.setId(2L);
        bond.setTicker("BOND1");
        bond.setQuantity(5);
        bond.setAssetType(AssetType.BOND);
        bond.setPurchasePrice(new BigDecimal("200.00"));
        bond.setPurchaseDate(LocalDate.now());

        when(portfolioItemRepository.findAll()).thenReturn(Arrays.asList(stock, bond));

        PortfolioSummaryResponse result = service.getSummary();

        assertNotNull(result);
        assertEquals(2, result.totalPositions());
        assertEquals(15, result.totalQuantity());
        assertTrue(result.quantityByAssetType().containsKey("STOCK"));
        assertTrue(result.quantityByAssetType().containsKey("BOND"));
        assertEquals(10L, result.quantityByAssetType().get("STOCK"));
        assertEquals(5L, result.quantityByAssetType().get("BOND"));
    }

    @Test
    @DisplayName("getEntity_validId_returnsItem")
    void testGetEntity_ValidId_ReturnsItem() {
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        PortfolioItem result = service.getEntity(1L);

        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
    }

    @Test
    @DisplayName("getEntity_invalidId_throwsResourceNotFoundException")
    void testGetEntity_InvalidId_ThrowsResourceNotFoundException() {
        when(portfolioItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getEntity(999L)
        );
    }

    @Test
    @DisplayName("create_maturityDateEqualsToday_throwsIllegalArgument")
    void testCreate_MaturityDateEqualsToday_ThrowsIllegalArgument() {
        LocalDate today = LocalDate.now();
        CreatePortfolioItemRequest request = new CreatePortfolioItemRequest(
                "BOND1",
                10,
                AssetType.BOND,
                new BigDecimal("100.00"),
                today,
                "Bond",
                null,
                null,
                null,
                today // maturity date equals purchase date
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );

        assertTrue(exception.getMessage().contains("maturityDate must be after purchaseDate"));
    }

    @Test
    @DisplayName("sell_transactionTotalCalculation_isCorrect")
    void testSell_TransactionTotalCalculation_IsCorrect() {
        testItem.setQuantity(10);
        when(portfolioItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal sellPrice = new BigDecimal("150.00");
        TransactionResponse result = service.sell(1L, sellPrice);

        assertNotNull(result.totalValue());
        // Total should be quantity * price = 10 * 150.00 = 1500.00
        assertEquals(new BigDecimal("1500.00"), result.totalValue());
    }
}



