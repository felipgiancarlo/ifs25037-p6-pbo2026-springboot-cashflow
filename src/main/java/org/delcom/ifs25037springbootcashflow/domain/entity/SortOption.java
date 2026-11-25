package org.delcom.ifs25037springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi.
 * Setiap opsi membawa comparator-nya sendiri sehingga logika pengurutan
 * terpusat di domain, bukan tersebar sebagai angka ajaib di view/use case.
 */
public enum SortOption {
    /** Transaksi terbaru (createdAt / id terbesar) di atas. Default UI. */
    NEWEST_FIRST(Comparator
            .comparing(Transaction::getCreatedAt, Comparator.nullsLast(Comparator.<java.time.LocalDateTime>naturalOrder()))
            .thenComparing(Transaction::getId)
            .reversed()),

    /** Transaksi terlama di atas. */
    OLDEST_FIRST(Comparator
            .comparing(Transaction::getCreatedAt, Comparator.nullsLast(Comparator.<java.time.LocalDateTime>naturalOrder()))
            .thenComparing(Transaction::getId)),

    /** Jumlah dari terkecil ke terbesar. */
    AMOUNT_ASC(Comparator.comparingLong(Transaction::getAmount)),

    /** Jumlah dari terbesar ke terkecil. */
    AMOUNT_DESC(Comparator.comparingLong(Transaction::getAmount).reversed()),

    /** Tampilkan pemasukan terlebih dahulu, lalu yang terbaru. */
    INCOME_FIRST(Comparator.comparing((Transaction t) -> t.getType() != TransactionType.INCOME)
            .thenComparing(Transaction::getId, Comparator.reverseOrder())),

    /** Tampilkan pengeluaran terlebih dahulu, lalu yang terbaru. */
    EXPENSE_FIRST(Comparator.comparing((Transaction t) -> t.getType() != TransactionType.EXPENSE)
            .thenComparing(Transaction::getId, Comparator.reverseOrder()));

    /** Comparator yang digunakan untuk mengurutkan daftar transaksi. */
    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    /** Mengembalikan comparator yang sesuai dengan opsi ini. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }

    /** Label singkat untuk ditampilkan di UI. */
    public String label() {
        return switch (this) {
            case NEWEST_FIRST -> "Terbaru";
            case OLDEST_FIRST -> "Terlama";
            case AMOUNT_ASC -> "Jumlah: kecil ke besar";
            case AMOUNT_DESC -> "Jumlah: besar ke kecil";
            case INCOME_FIRST -> "Pemasukan dulu";
            case EXPENSE_FIRST -> "Pengeluaran dulu";
        };
    }
}
