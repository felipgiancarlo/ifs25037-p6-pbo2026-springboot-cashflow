package org.delcom.ifs25037springbootcashflow.domain.entity;

/**
 * Filter jenis transaksi untuk daftar yang ditampilkan di UI.
 */
public enum TransactionTypeFilter {
    ALL,
    INCOME,
    EXPENSE;

    /** Label tampilan dalam Bahasa Indonesia. */
    public String label() {
        return switch (this) {
            case ALL -> "Semua tipe";
            case INCOME -> "Pemasukan";
            case EXPENSE -> "Pengeluaran";
        };
    }

    /** Menentukan apakah transaksi lolos filter ini. */
    public boolean matches(Transaction transaction) {
        return switch (this) {
            case ALL -> true;
            case INCOME -> transaction.getType() == TransactionType.INCOME;
            case EXPENSE -> transaction.getType() == TransactionType.EXPENSE;
        };
    }
}
