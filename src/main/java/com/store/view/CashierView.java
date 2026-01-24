package com.store.view;

import com.store.model.Bill;
import com.store.model.Product;
import com.store.util.IOHandler;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class CashierView {

    private List<Product> products;
    private List<Bill> salesHistory; // Список продаж
    private String cashierName;

    public CashierView(List<Product> products, String cashierName) {
        this.products = products;
        this.cashierName = cashierName;
        // Загружаем прошлые продажи, чтобы не стереть историю
        this.salesHistory = IOHandler.load("sales.dat");
        if (this.salesHistory == null) this.salesHistory = new ArrayList<>();
    }

    public VBox getView() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));

        Label header = new Label("CASHIER POS System");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<Product> productBox = new ComboBox<>(FXCollections.observableArrayList(products));
        productBox.setPromptText("Select Product");
        productBox.setMaxWidth(300);

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");

        Button sellBtn = new Button("PROCESS SALE");
        sellBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        sellBtn.setOnAction(e -> {
            Product selected = productBox.getValue();
            if (selected == null || qtyField.getText().isEmpty()) {
                showAlert("Error", "Select product and quantity!");
                return;
            }

            try {
                int qty = Integer.parseInt(qtyField.getText());

                if (selected.getStockQuantity() >= qty) {
                    // 1. Списываем со склада
                    selected.setStockQuantity(selected.getStockQuantity() - qty);
                    IOHandler.save("products.dat", products);

                    // 2. Считаем сумму
                    double total = selected.getPrice() * qty;

                    // 3. Создаем чек
                    Bill newBill = new Bill(cashierName, selected.getName(), qty, total);

                    // 4. Сохраняем чек в ТЕКСТОВЫЙ файл (для печати)
                    String txtFileName = "Bill_" + newBill.getBillId();
                    IOHandler.printBill(newBill.getReceiptContent(), txtFileName);

                    // 5. Сохраняем чек в ИСТОРИЮ (для твоего фин. отчета)
                    salesHistory.add(newBill);
                    IOHandler.save("sales.dat", salesHistory);

                    showAlert("Success", "Sale Complete! Receipt saved: " + txtFileName + ".txt");
                    qtyField.clear();
                } else {
                    showAlert("Error", "Not enough stock! Only " + selected.getStockQuantity() + " left.");
                }
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid quantity!");
            }
        });

        pane.getChildren().addAll(header, new Label("Item:"), productBox, new Label("Qty:"), qtyField, sellBtn);
        return pane;
    }

    private void showAlert(String title, String content) {
        new Alert(Alert.AlertType.INFORMATION, content).showAndWait();
    }
}