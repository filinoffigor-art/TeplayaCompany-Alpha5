package ru.teplayakompaniya.tk4;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import org.junit.Test;

public class FinanceRulesTest {

    @Test public void turnoverCountsOnlyIncome() {
        assertEquals(300L, FinanceRules.turnover(Arrays.asList(
                new FinanceRules.MoneyOperation("INCOME", 100),
                new FinanceRules.MoneyOperation("TRANSFER", 500),
                new FinanceRules.MoneyOperation("EXPENSE", 50),
                new FinanceRules.MoneyOperation("INCOME", 200)
        )));
    }

    @Test public void internalTransferIsNotExpense() {
        assertEquals(50L, FinanceRules.companyExpense(Arrays.asList(
                new FinanceRules.MoneyOperation("TRANSFER", 500),
                new FinanceRules.MoneyOperation("EXPENSE", 50)
        )));
    }

    @Test public void debtOnlyOverdueUnpaidPart() {
        LocalDate today = LocalDate.of(2026, 9, 12);
        assertEquals(150L, FinanceRules.debt(Arrays.asList(
                new FinanceRules.PaymentStage(LocalDate.of(2026, 9, 10), 200, 50),
                new FinanceRules.PaymentStage(LocalDate.of(2026, 9, 15), 300, 0),
                new FinanceRules.PaymentStage(LocalDate.of(2026, 9, 9), 100, 100)
        ), today));
    }

    @Test public void plannedReceiptsOnlyFutureAndTodayUnpaid() {
        LocalDate today = LocalDate.of(2026, 9, 12);
        assertEquals(350L, FinanceRules.plannedReceipts(Arrays.asList(
                new FinanceRules.PaymentStage(LocalDate.of(2026, 9, 10), 200, 50),
                new FinanceRules.PaymentStage(LocalDate.of(2026, 9, 12), 100, 50),
                new FinanceRules.PaymentStage(LocalDate.of(2026, 9, 15), 300, 0)
        ), today));
    }

    @Test public void installerDueSubtractsAdvanceAndPayments() {
        assertEquals(62000L, FinanceRules.installerDue(92000L, 30000L));
    }

    @Test public void accountableTransferPreservesCompanyTotal() {
        long a = FinanceRules.accountableBalance(420000, 0, 0, 0, 50000);
        long b = FinanceRules.accountableBalance(386000, 0, 0, 50000, 0);
        assertEquals(806000L, a + b);
    }
}
