package projek_keuangan.item;

import javafx.beans.property.SimpleStringProperty;

public class TargetItem {
    private final SimpleStringProperty month;
    private final SimpleStringProperty targetAmount;
    private final SimpleStringProperty actualExpense;
    private final SimpleStringProperty status;

    public TargetItem(String month, String targetAmount, String actualExpense, String status) {
        this.month = new SimpleStringProperty(month);
        this.targetAmount = new SimpleStringProperty(targetAmount);
        this.actualExpense = new SimpleStringProperty(actualExpense);
        this.status = new SimpleStringProperty(status);
    }

    // Getter dan property methods
    public String getMonth() { return month.get(); }
    public SimpleStringProperty monthProperty() { return month; }

    public String getTargetAmount() { return targetAmount.get(); }
    public SimpleStringProperty targetAmountProperty() { return targetAmount; }

    public String getActualExpense() { return actualExpense.get(); }
    public SimpleStringProperty actualExpenseProperty() { return actualExpense; }

    public String getStatus() { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }
}