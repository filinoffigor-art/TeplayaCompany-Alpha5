package ru.teplayakompaniya.tk4;

import java.time.LocalDate;
import java.util.List;

public final class FinanceRules {

    private FinanceRules() {}

    public static long turnover(List<MoneyOperation> ops) {
        long total = 0;
        for (MoneyOperation op : ops) {
            if ("INCOME".equals(op.type)) total += op.amount;
        }
        return total;
    }

    public static long companyExpense(List<MoneyOperation> ops) {
        long total = 0;
        for (MoneyOperation op : ops) {
            if ("EXPENSE".equals(op.type)) total += op.amount;
        }
        return total;
    }

    public static long debt(List<PaymentStage> stages, LocalDate today) {
        long total = 0;
        for (PaymentStage s : stages) {
            long unpaid = Math.max(0, s.plannedAmount - s.actualAmount);
            if (unpaid > 0 && s.plannedDate.isBefore(today)) total += unpaid;
        }
        return total;
    }

    public static long plannedReceipts(List<PaymentStage> stages, LocalDate today) {
        long total = 0;
        for (PaymentStage s : stages) {
            long unpaid = Math.max(0, s.plannedAmount - s.actualAmount);
            if (unpaid > 0 && !s.plannedDate.isBefore(today)) total += unpaid;
        }
        return total;
    }

    public static long installerDue(long accrued, long paidAdvancesAndPayments) {
        return Math.max(0, accrued - paidAdvancesAndPayments);
    }

    public static long accountableBalance(long opening, long income, long expenses, long incomingTransfers, long outgoingTransfers) {
        return opening + income - expenses + incomingTransfers - outgoingTransfers;
    }

    public static final class MoneyOperation {
        public final String type; // INCOME, EXPENSE, TRANSFER
        public final long amount;
        public MoneyOperation(String type, long amount) {
            this.type = type;
            this.amount = amount;
        }
    }

    public static final class PaymentStage {
        public final LocalDate plannedDate;
        public final long plannedAmount;
        public final long actualAmount;
        public PaymentStage(LocalDate plannedDate, long plannedAmount, long actualAmount) {
            this.plannedDate = plannedDate;
            this.plannedAmount = plannedAmount;
            this.actualAmount = actualAmount;
        }
    }
}
