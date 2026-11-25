package org.delcom.ifs25037springbootcashflow.usecase;

import org.delcom.ifs25037springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25037springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25037springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25037springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25037springbootcashflow.domain.repository.IFinanceAiService;
import org.delcom.ifs25037springbootcashflow.domain.repository.ITransactionRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case yang menangani logika pembuatan transaksi menggunakan AI.
 * Tidak mengimpor Vaadin / HTTP; semua dependensi lewat port di domain.
 */
public class FinanceAiUseCase {

    private final IFinanceAiService aiService;
    private final ITransactionRepository transactionRepository;

    public FinanceAiUseCase(IFinanceAiService aiService, ITransactionRepository transactionRepository) {
        this.aiService = aiService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Meminta AI menghasilkan daftar transaksi berdasarkan catatan keuangan bebas.
     *
     * @param prompt instruksi dari pengguna
     * @return daftar transaksi hasil generasi (belum disimpan)
     * @throws DomainException jika validasi prompt gagal atau AI bermasalah
     */
    public List<AiGeneratedTransaction> generateTransactions(String prompt) throws DomainException {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new ValidationException("Prompt catatan keuangan tidak boleh kosong");
        }
        return aiService.generateTransactions(prompt.trim());
    }

    /**
     * Menyimpan transaksi hasil generasi AI ke basis data untuk pengguna tertentu.
     *
     * @param userId ID pengguna pemilik
     * @param items  daftar transaksi yang akan disimpan
     * @return daftar entitas transaksi yang berhasil disimpan
     * @throws ValidationException jika daftar item kosong
     */
    public List<Transaction> saveGeneratedTransactions(int userId, List<AiGeneratedTransaction> items)
            throws ValidationException {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Daftar transaksi hasil generate tidak boleh kosong");
        }
        List<Transaction> saved = new ArrayList<>();
        for (AiGeneratedTransaction item : items) {
            saved.add(transactionRepository.save(userId, item.description(), item.amount(), item.type()));
        }
        return saved;
    }
}
