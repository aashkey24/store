package com.store.main;

import com.store.model.*;
import com.store.util.IOHandler;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ElectronicStoreApp extends Application {
    private List<User> users = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private Stage stage;
    private User currentUser;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        loadData();
        showLogin();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        // Загрузка пользователей
        List<User> loadedUsers = (List<User>) IOHandler.load("users.dat");

        if (loadedUsers != null && !loadedUsers.isEmpty()) {
            users = loadedUsers;
        } else {
            // Создаем ВСЕХ сотрудников по умолчанию
            users.add(new Admin("admin", "admin1234", "System Administrator"));
            users.add(new Manager("manager", "manager123", "Ilias Manager"));
            users.add(new Cashier("cashier", "cashier123", "Abdulaziz Cashier"));

            IOHandler.save("users.dat", users);
        }

        // Загрузка товаров
        List<Product> loadedProds = (List<Product>) IOHandler.load("products.dat");
        if (loadedProds != null) {
            products = loadedProds;
        } else {
            // Добавим пару товаров для теста
            products.add(new Product("iPhone 15", 999.99, 10));
            products.add(new Product("Laptop HP", 550.00, 2)); // Мало на складе
            IOHandler.save("products.dat", products);
        }
    }

    // --- ЭКРАН ЛОГИНА ---
    private void showLogin() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f4f4f4;");

        Label title = new Label("ELECTRONICS STORE LOGIN");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField uField = new TextField();
        uField.setPromptText("Username");
        uField.setMaxWidth(250);

        PasswordField pField = new PasswordField();
        pField.setPromptText("Password");
        pField.setMaxWidth(250);

        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        loginBtn.setMinWidth(150);

        loginBtn.setOnAction(e -> {
            boolean found = false;
            for (User u : users) {
                if (u.getUsername().equals(uField.getText()) && u.getPassword().equals(pField.getText())) {
                    currentUser = u;
                    showDashboard();
                    found = true;
                    break;
                }
            }
            if (!found) {
                new Alert(Alert.AlertType.ERROR, "Invalid Username or Password!").show();
            }
        });

        root.getChildren().addAll(title, uField, pField, loginBtn);
        stage.setScene(new Scene(root, 400, 350));
        stage.setTitle("Login - Electronics Store");
        stage.show();
    }

    // --- ГЛАВНОЕ МЕНЮ (DASHBOARD) ---
    private void showDashboard() {
        BorderPane root = new BorderPane();

        // Боковая панель
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(220);

        Label welcome = new Label("Welcome,\n" + currentUser.getFullName());
        welcome.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label role = new Label("[" + currentUser.getRole() + "]");
        role.setStyle("-fx-text-fill: #bdc3c7;");

        sidebar.getChildren().addAll(welcome, role, new Separator());

        // Центральная часть
        StackPane content = new StackPane();
        content.getChildren().add(new Label("Select an option from the sidebar."));

        // Логика кнопок для разных ролей
        if (currentUser instanceof Admin) {
            Button btn = createNavButton("Manage Staff");
            btn.setOnAction(e -> content.getChildren().setAll(createAdminPane()));
            sidebar.getChildren().add(btn);
        }

        if (currentUser instanceof Manager || currentUser instanceof Admin) {
            Button btn = createNavButton("Inventory Management");
            btn.setOnAction(e -> content.getChildren().setAll(createManagerPane()));
            sidebar.getChildren().add(btn);
        }

        if (currentUser instanceof Cashier || currentUser instanceof Admin) {
            Button btn = createNavButton("New Sale (POS)");
            btn.setOnAction(e -> content.getChildren().setAll(createCashierPane()));
            sidebar.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Logout");
        logout.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(e -> showLogin());

        sidebar.getChildren().addAll(spacer, logout);
        root.setLeft(sidebar);
        root.setCenter(content);

        stage.setScene(new Scene(root, 950, 650));
        stage.setTitle("Dashboard - " + currentUser.getRole());
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-alignment: center-left; -fx-padding: 10;");
        return btn;
    }

    // --- ПАНЕЛЬ АДМИНИСТРАТОРА ---
    private VBox createAdminPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        Label header = new Label("STAFF MANAGEMENT");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<User> userList = new ListView<>(FXCollections.observableArrayList(users));

        TextField nameField = new TextField(); nameField.setPromptText("Full Name");
        TextField userField = new TextField(); userField.setPromptText("Username");
        TextField passField = new TextField(); passField.setPromptText("Password");

        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("Manager", "Cashier", "Administrator"));
        roleBox.setValue("Cashier");

        Button addBtn = new Button("Add User");
        addBtn.setOnAction(e -> {
            if (nameField.getText().isEmpty() || userField.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please fill all fields").show();
                return;
            }

            String r = roleBox.getValue();
            User newUser;
            if (r.equals("Manager")) newUser = new Manager(userField.getText(), passField.getText(), nameField.getText());
            else if (r.equals("Administrator")) newUser = new Admin(userField.getText(), passField.getText(), nameField.getText());
            else newUser = new Cashier(userField.getText(), passField.getText(), nameField.getText());

            users.add(newUser);
            IOHandler.save("users.dat", users);
            userList.setItems(FXCollections.observableArrayList(users));
            new Alert(Alert.AlertType.INFORMATION, "User added successfully!").show();
        });

        pane.getChildren().addAll(header, userList, new Separator(),
                new Label("Register New Employee:"), nameField, userField, passField, roleBox, addBtn);
        return pane;
    }

    // --- ПАНЕЛЬ МЕНЕДЖЕРА ---
    private VBox createManagerPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        Label header = new Label("INVENTORY & STOCK");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<Product> productList = new ListView<>(FXCollections.observableArrayList(products));

        TextField pName = new TextField(); pName.setPromptText("Product Name");
        TextField pPrice = new TextField(); pPrice.setPromptText("Price");
        TextField pStock = new TextField(); pStock.setPromptText("Initial Stock");

        Button addBtn = new Button("Add / Restock Product");
        addBtn.setOnAction(e -> {
            try {
                String name = pName.getText();
                double price = Double.parseDouble(pPrice.getText());
                int stock = Integer.parseInt(pStock.getText());

                products.add(new Product(name, price, stock));
                IOHandler.save("products.dat", products);
                productList.setItems(FXCollections.observableArrayList(products));
                new Alert(Alert.AlertType.INFORMATION, "Inventory Updated!").show();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid Number Format!").show();
            }
        });

        // Бонусная функция: Уведомление о малом остатке
        Button alertBtn = new Button("Check Low Stock");
        alertBtn.setOnAction(e -> {
            long lowStockCount = products.stream().filter(p -> p.getStockQuantity() < 3).count();
            if (lowStockCount > 0) {
                new Alert(Alert.AlertType.WARNING, "Warning! " + lowStockCount + " items are running low (<3 units).").show();
            } else {
                new Alert(Alert.AlertType.INFORMATION, "All stock levels are healthy.").show();
            }
        });

        pane.getChildren().addAll(header, productList, new Separator(),
                new Label("New Product Details:"), pName, pPrice, pStock, addBtn, alertBtn);
        return pane;
    }

    // --- ПАНЕЛЬ КАССИРА (С ИСПОЛЬЗОВАНИЕМ КЛАССА BILL) ---
    private VBox createCashierPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        Label header = new Label("CASHIER POINT OF SALE");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<Product> productBox = new ComboBox<>(FXCollections.observableArrayList(products));
        productBox.setPromptText("Select Item to Sell");
        productBox.setMaxWidth(300);

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");
        qtyField.setMaxWidth(100);

        Button sellBtn = new Button("CONFIRM SALE");
        sellBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        sellBtn.setOnAction(e -> {
            Product selected = productBox.getValue();
            if (selected == null || qtyField.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Please select a product and enter quantity.").show();
                return;
            }

            try {
                int qty = Integer.parseInt(qtyField.getText());

                if (selected.getStockQuantity() >= qty) {
                    // 1. Обновляем склад
                    selected.setStockQuantity(selected.getStockQuantity() - qty);
                    IOHandler.save("products.dat", products);

                    // 2. Создаем чек (через класс Bill)
                    double total = selected.getPrice() * qty;
                    Bill newBill = new Bill(currentUser.getFullName(), selected.getName(), qty, total);

                    // 3. Сохраняем чек в файл
                    String fileName = "Bill_" + newBill.getBillId();
                    IOHandler.printBill(newBill.getReceiptContent(), fileName);

                    new Alert(Alert.AlertType.INFORMATION, "Sale Successful! Receipt saved to " + fileName + ".txt").show();

                    // Сброс полей
                    qtyField.clear();
                    productBox.getSelectionModel().clearSelection();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Insufficient Stock! Only " + selected.getStockQuantity() + " left.").show();
                }
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Quantity must be a valid number!").show();
            }
        });

        pane.getChildren().addAll(header, new Label("Item:"), productBox, new Label("Qty:"), qtyField, sellBtn);
        return pane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}