package com.store.view;

import com.store.model.Product;
import com.store.util.IOHandler;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class ManagerView {

    private List<Product> products;

    public ManagerView(List<Product> products) {
        this.products = products;
    }

    public VBox getView() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));

        Label header = new Label("MANAGER PANEL: Inventory");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Таблица товаров
        ListView<Product> productList = new ListView<>(FXCollections.observableArrayList(products));

        // Поля для добавления
        TextField pName = new TextField(); pName.setPromptText("Product Name");
        TextField pPrice = new TextField(); pPrice.setPromptText("Price");
        TextField pStock = new TextField(); pStock.setPromptText("Stock Qty");
        TextField pSupplier = new TextField(); pSupplier.setPromptText("Supplier");

        Button addBtn = new Button("Add / Restock");
        addBtn.setOnAction(e -> {
            try {
                String name = pName.getText();
                double price = Double.parseDouble(pPrice.getText());
                int stock = Integer.parseInt(pStock.getText());

                // Проверка: если товар уже есть, просто обновляем количество
                boolean exists = false;
                for (Product p : products) {
                    if (p.getName().equalsIgnoreCase(name)) {
                        p.setStockQuantity(p.getStockQuantity() + stock);
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    products.add(new Product(name, price, stock));
                }

                IOHandler.save("products.dat", products);
                productList.setItems(FXCollections.observableArrayList(products));
                showAlert("Success", "Inventory updated!");

            } catch (NumberFormatException ex) {
                showAlert("Error", "Price and Stock must be numbers!");
            }
        });

        // БОНУС: Кнопка проверки товаров, которые заканчиваются (Requirement: < 3 units)
        Button checkStockBtn = new Button("Check Low Stock Alerts");
        checkStockBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        checkStockBtn.setOnAction(e -> {
            List<String> lowStockItems = products.stream()
                    .filter(p -> p.getStockQuantity() < 3)
                    .map(p -> p.getName() + " (" + p.getStockQuantity() + " left)")
                    .collect(Collectors.toList());

            if (lowStockItems.isEmpty()) {
                showAlert("Stock Status", "All items are well stocked.");
            } else {
                showAlert("WARNING: LOW STOCK", "Refill needed for:\n" + String.join("\n", lowStockItems));
            }
        });

        pane.getChildren().addAll(header, productList, new Label("New Product / Restock:"),
                pName, pPrice, pStock, pSupplier, addBtn, new Separator(), checkStockBtn);
        return pane;
    }

    private void showAlert(String title, String content) {
        new Alert(Alert.AlertType.INFORMATION, content).showAndWait();
    }
}