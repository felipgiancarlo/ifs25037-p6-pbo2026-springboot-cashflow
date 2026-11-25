package org.delcom.ifs25037springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private final Transaction small = new Transaction(1, 1, "kecil", 1_000, TransactionType.EXPENSE);
    private final Transaction big = new Transaction(2, 1, "besar", 9_000, TransactionType.INCOME);
    private final Transaction mid = new Transaction(3, 1, "sedang", 5_000, TransactionType.EXPENSE);

    private List<Integer> sortedIds(SortOption option) {
        List<Transaction> list = new ArrayList<>(List.of(small, big, mid));
        list.sort(option.comparator());
        return list.stream().map(Transaction::getId).toList();
    }

    @Test
    void amountAscending() {
        assertEquals(List.of(1, 3, 2), sortedIds(SortOption.AMOUNT_ASC));
    }

    @Test
    void amountDescending() {
        assertEquals(List.of(2, 3, 1), sortedIds(SortOption.AMOUNT_DESC));
    }

    @Test
    void incomeFirst() {
        assertEquals(2, sortedIds(SortOption.INCOME_FIRST).get(0));
    }

    @Test
    void expenseFirst() {
        assertEquals(2, sortedIds(SortOption.EXPENSE_FIRST).get(2));
    }

    @Test
    void hasAtLeastFourOptionsIncludingNewestAndOldest() {
        assertEquals(6, SortOption.values().length);
        assertEquals(SortOption.NEWEST_FIRST, SortOption.values()[0]);
    }

    @Test
    void newestAndOldestUseCreatedAtThenId() {
        java.time.LocalDateTime t0 = java.time.LocalDateTime.of(2026, 1, 1, 10, 0);
        Transaction old = new Transaction(1, 1, "lama", 1, TransactionType.INCOME, null, t0);
        Transaction recent = new Transaction(2, 1, "baru", 1, TransactionType.INCOME, null, t0.plusDays(1));
        Transaction sameTimeHigherId = new Transaction(3, 1, "sama", 1, TransactionType.INCOME, null, t0.plusDays(1));
        Transaction noDate = new Transaction(4, 1, "tanpa", 1, TransactionType.INCOME, null, null);

        List<Transaction> newest = new ArrayList<>(List.of(old, recent, sameTimeHigherId));
        newest.sort(SortOption.NEWEST_FIRST.comparator());
        assertEquals(List.of(3, 2, 1), newest.stream().map(Transaction::getId).toList());

        List<Transaction> oldest = new ArrayList<>(List.of(sameTimeHigherId, recent, old));
        oldest.sort(SortOption.OLDEST_FIRST.comparator());
        assertEquals(List.of(1, 2, 3), oldest.stream().map(Transaction::getId).toList());

        List<Transaction> withNull = new ArrayList<>(List.of(noDate, old));
        withNull.sort(SortOption.OLDEST_FIRST.comparator());
        assertEquals(4, withNull.get(1).getId());
    }

    @Test
    void everyOptionHasLabel() {
        for (SortOption option : SortOption.values()) {
            assertEquals(false, option.label().isBlank());
        }
        assertEquals("Terbaru", SortOption.NEWEST_FIRST.label());
        assertEquals("Terlama", SortOption.OLDEST_FIRST.label());
    }
}
