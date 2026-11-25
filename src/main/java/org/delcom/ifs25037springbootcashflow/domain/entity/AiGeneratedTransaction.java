package org.delcom.ifs25037springbootcashflow.domain.entity;

/**
 * Representasi transaksi hasil generasi AI sebelum disimpan ke basis data.
 *
 * @param description keterangan transaksi
 * @param amount      jumlah uang (selalu positif)
 * @param type        jenis transaksi (pemasukan / pengeluaran)
 */
public record AiGeneratedTransaction(String description, long amount, TransactionType type) {

    public AiGeneratedTransaction {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Keterangan transaksi tidak boleh kosong");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Jumlah transaksi harus lebih dari 0");
        }
        if (type == null) {
            throw new IllegalArgumentException("Jenis transaksi tidak boleh kosong");
        }
        description = description.trim();
    }
}
