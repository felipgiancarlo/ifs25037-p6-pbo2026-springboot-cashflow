package org.delcom.ifs25037springbootcashflow.adapter.ai;

import org.delcom.ifs25037springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25037springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25037springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25037springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25037springbootcashflow.domain.repository.IFinanceAiService;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementasi in-memory {@link IFinanceAiService} untuk pengujian unit deterministik tanpa jaringan.
 */
public class InMemoryFinanceAiService implements IFinanceAiService {

    private final List<AiGeneratedTransaction> cannedResponses = new ArrayList<>();
    private boolean simulateFailure = false;

    public InMemoryFinanceAiService() {
    }

    public InMemoryFinanceAiService(List<AiGeneratedTransaction> defaultResponses) {
        if (defaultResponses != null) {
            this.cannedResponses.addAll(defaultResponses);
        }
    }

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    public void addCannedResponse(String description, long amount, TransactionType type) {
        this.cannedResponses.add(new AiGeneratedTransaction(description, amount, type));
    }

    @Override
    public List<AiGeneratedTransaction> generateTransactions(String prompt) throws DomainException {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new ValidationException("Prompt catatan keuangan tidak boleh kosong");
        }
        if (simulateFailure) {
            throw new DomainException("Simulasi kegagalan layanan AI");
        }
        if (!cannedResponses.isEmpty()) {
            return new ArrayList<>(cannedResponses);
        }
        return List.of(
                new AiGeneratedTransaction("Pemasukan AI: " + prompt.trim(), 1_000_000, TransactionType.INCOME),
                new AiGeneratedTransaction("Pengeluaran AI: " + prompt.trim(), 250_000, TransactionType.EXPENSE)
        );
    }
}
