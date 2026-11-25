package org.delcom.ifs25037springbootcashflow.adapter.ai;

import org.delcom.ifs25037springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25037springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25037springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25037springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryFinanceAiServiceTest {

    @Test
    void returnsDeterministicDefaultsWhenNoCannedResponses() throws Exception {
        List<AiGeneratedTransaction> result = new InMemoryFinanceAiService().generateTransactions(" kopi ");
        assertEquals(2, result.size());
        assertEquals(TransactionType.INCOME, result.get(0).type());
        assertEquals(TransactionType.EXPENSE, result.get(1).type());
        assertEquals("Pengeluaran AI: kopi", result.get(1).description());
    }

    @Test
    void returnsCannedResponses() throws Exception {
        InMemoryFinanceAiService service = new InMemoryFinanceAiService();
        service.addCannedResponse("Gaji", 100, TransactionType.INCOME);
        List<AiGeneratedTransaction> result = service.generateTransactions("x");
        assertEquals(1, result.size());
        assertEquals("Gaji", result.get(0).description());
    }

    @Test
    void constructorAcceptsDefaultResponsesOrNull() throws Exception {
        InMemoryFinanceAiService withList = new InMemoryFinanceAiService(
                List.of(new AiGeneratedTransaction("A", 1, TransactionType.EXPENSE)));
        assertEquals(1, withList.generateTransactions("x").size());
        assertEquals(2, new InMemoryFinanceAiService(null).generateTransactions("x").size());
    }

    @Test
    void rejectsBlankPromptAndSimulatesFailure() {
        InMemoryFinanceAiService service = new InMemoryFinanceAiService();
        assertThrows(ValidationException.class, () -> service.generateTransactions(null));
        assertThrows(ValidationException.class, () -> service.generateTransactions("  "));
        service.setSimulateFailure(true);
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
    }
}
