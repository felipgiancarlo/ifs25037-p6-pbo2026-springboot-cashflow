package org.delcom.ifs25037springbootcashflow.usecase;

import org.delcom.ifs25037springbootcashflow.adapter.ai.InMemoryFinanceAiService;
import org.delcom.ifs25037springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25037springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25037springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25037springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25037springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25037springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinanceAiUseCaseTest {

    private InMemoryFinanceAiService aiService;
    private TransactionRepository repository;
    private FinanceAiUseCase useCase;

    @BeforeEach
    void setUp() {
        aiService = new InMemoryFinanceAiService();
        repository = new TransactionRepository();
        useCase = new FinanceAiUseCase(aiService, repository);
    }

    @Test
    void generateTransactionsReturnsList() throws Exception {
        List<AiGeneratedTransaction> result = useCase.generateTransactions("  gajian 5 juta  ");
        assertEquals(2, result.size());
        assertEquals("Pemasukan AI: gajian 5 juta", result.get(0).description());
    }

    @Test
    void generateTransactionsRejectsBlankPrompt() {
        assertThrows(ValidationException.class, () -> useCase.generateTransactions(null));
        assertThrows(ValidationException.class, () -> useCase.generateTransactions("   "));
    }

    @Test
    void generateTransactionsPropagatesAiFailure() {
        aiService.setSimulateFailure(true);
        assertThrows(DomainException.class, () -> useCase.generateTransactions("apa saja"));
    }

    @Test
    void saveGeneratedTransactionsPersistsToRepository() throws Exception {
        List<AiGeneratedTransaction> items = List.of(
                new AiGeneratedTransaction("Gaji", 5_000_000, TransactionType.INCOME),
                new AiGeneratedTransaction("Makan", 25_000, TransactionType.EXPENSE)
        );

        List<Transaction> saved = useCase.saveGeneratedTransactions(1, items);
        assertEquals(2, saved.size());
        assertEquals(2, repository.findByUserId(1).size());
        assertEquals(TransactionType.EXPENSE, saved.get(1).getType());
    }

    @Test
    void saveGeneratedTransactionsRejectsEmptyOrNull() {
        assertThrows(ValidationException.class, () -> useCase.saveGeneratedTransactions(1, null));
        assertThrows(ValidationException.class,
                () -> useCase.saveGeneratedTransactions(1, Collections.emptyList()));
    }
}
