package com.example.hsbcproject.support;
import com.example.hsbcproject.domain.AssetType;
import com.example.hsbcproject.domain.DividendRecord;
import com.example.hsbcproject.domain.PortfolioItem;
import com.example.hsbcproject.domain.PortfolioSnapshot;
import com.example.hsbcproject.domain.Transaction;
import com.example.hsbcproject.domain.TransactionType;
import com.example.hsbcproject.domain.WatchlistItem;
import com.example.hsbcproject.dto.CreateDividendRequest;
import com.example.hsbcproject.dto.CreatePortfolioItemRequest;
import com.example.hsbcproject.dto.CreateTransactionRequest;
import com.example.hsbcproject.dto.CreateWatchlistItemRequest;
import com.example.hsbcproject.dto.UpdatePortfolioItemRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public final class TestDataFactory {
    private TestDataFactory() {
    }
    public static PortfolioItem portfolioItem() {
        PortfolioItem item = new PortfolioItem();
        item.setId(1L);
        item.setTicker("AAPL");
        item.setQuantity(new BigDecimal("10.00000000"));
        item.setAssetType(AssetType.STOCK);
        item.setPurchasePrice(new BigDecimal("100.00"));
        item.setPurchaseDate(LocalDate.now().minusDays(120));
        item.setName("Apple Inc.");
        item.setSector("Technology");
        return item;
    }
    public static PortfolioItem secondPortfolioItem() {
        PortfolioItem item = new PortfolioItem();
        item.setId(2L);
        item.setTicker("MSFT");
        item.setQuantity(new BigDecimal("5.00000000"));
        item.setAssetType(AssetType.STOCK);
        item.setPurchasePrice(new BigDecimal("200.00"));
        item.setPurchaseDate(LocalDate.now().minusDays(60));
        item.setName("Microsoft");
        item.setSector("Technology");
        return item;
    }
    public static PortfolioItem bondItem() {
        PortfolioItem item = new PortfolioItem();
        item.setId(3L);
        item.setTicker("AGG");
        item.setQuantity(new BigDecimal("5.00000000"));
        item.setAssetType(AssetType.BOND);
        item.setPurchasePrice(new BigDecimal("98.75"));
        item.setPurchaseDate(LocalDate.now().minusMonths(2));
        item.setIssuer("iShares");
        item.setMaturityDate(LocalDate.now().plusYears(2));
        return item;
    }
    public static CreatePortfolioItemRequest createPortfolioItemRequest() {
        return new CreatePortfolioItemRequest(
                "aapl",
                new BigDecimal("10.00000000"),
                AssetType.STOCK,
                new BigDecimal("100.00"),
                LocalDate.now().minusDays(120),
                "Apple Inc.",
                "Technology",
                null,
                null,
                null);
    }
    public static UpdatePortfolioItemRequest updatePortfolioItemRequest() {
        return new UpdatePortfolioItemRequest(
                "msft",
                new BigDecimal("15.00000000"),
                AssetType.STOCK,
                new BigDecimal("150.00"),
                LocalDate.now().minusDays(90),
                "Microsoft",
                "Technology",
                null,
                null,
                null);
    }
    public static Transaction transaction() {
        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setTicker("AAPL");
        tx.setAssetType(AssetType.STOCK);
        tx.setTransactionType(TransactionType.BUY);
        tx.setQuantity(new BigDecimal("10.00000000"));
        tx.setPricePerUnit(new BigDecimal("100.00"));
        tx.setTransactionDate(LocalDate.now().minusDays(120));
        tx.setNotes("Initial buy");
        return tx;
    }
    public static CreateTransactionRequest createTransactionRequest() {
        return new CreateTransactionRequest(
                "msft",
                AssetType.STOCK,
                TransactionType.SELL,
                new BigDecimal("2.50000000"),
                new BigDecimal("220.00"),
                LocalDate.now().minusDays(1),
                "Profit booking");
    }
    public static DividendRecord dividendRecord() {
        DividendRecord record = new DividendRecord();
        record.setId(1L);
        record.setTicker("AAPL");
        record.setDividendPerShare(new BigDecimal("0.50"));
        record.setSharesHeld(10);
        record.setTotalDividend(new BigDecimal("5.00"));
        record.setDividendDate(LocalDate.now().minusDays(10));
        return record;
    }
    public static CreateDividendRequest createDividendRequest() {
        return new CreateDividendRequest(
                "msft",
                new BigDecimal("0.75"),
                20,
                LocalDate.now().minusDays(5));
    }
    public static WatchlistItem watchlistItem() {
        WatchlistItem item = new WatchlistItem();
        item.setId(1L);
        item.setTicker("GOOGL");
        item.setAssetType(AssetType.STOCK);
        item.setAddedDate(LocalDate.now().minusDays(3));
        return item;
    }
    public static CreateWatchlistItemRequest createWatchlistItemRequest() {
        return new CreateWatchlistItemRequest("eth", AssetType.CRYPTO);
    }
    public static PortfolioSnapshot snapshot(LocalDate date, BigDecimal value) {
        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setId(Math.abs((long) date.hashCode()));
        snapshot.setSnapshotDate(date);
        snapshot.setTotalValue(value);
        snapshot.setTotalCostBasis(new BigDecimal("1000.00"));
        snapshot.setTotalGainLoss(value.subtract(new BigDecimal("1000.00")));
        snapshot.setTotalGainLossPct(new BigDecimal("10.00"));
        snapshot.setTotalPositions(2L);
        snapshot.setTotalQuantity(new BigDecimal("15.00000000"));
        snapshot.setCreatedAt(date);
        return snapshot;
    }
    public static List<PortfolioSnapshot> snapshots() {
        return List.of(
                snapshot(LocalDate.now().minusDays(2), new BigDecimal("1100.00")),
                snapshot(LocalDate.now().minusDays(1), new BigDecimal("1200.00")),
                snapshot(LocalDate.now(), new BigDecimal("1250.00")));
    }
}
