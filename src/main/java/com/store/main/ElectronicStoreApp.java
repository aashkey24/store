package com.store.main;

import com.store.model.*;
import com.store.util.IOHandler;
import com.store.view.AdminView;
import com.store.view.CashierView;
import com.store.view.ManagerView;
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
//
    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        loadData();
        showLogin();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        // Загрузка пользователей
        // Java сама поймет, что T = User, потому что слева написано List<User>
        List<User> loadedUsers = IOHandler.load("users.dat");

        if (loadedUsers != null && !loadedUsers.isEmpty()) {
            users = loadedUsers;
        } else {
            // Создаем ВСЕХ сотрудников по умолчанию
            users.add(new Admin("admin", "123", "Name", "555-0101", 2000.0));
            users.add(new Manager("manager", "123", "Name", "555-0101", 2000.0, "General"));
            users.add(new Cashier("cashier", "123", "Name", "555-0101", 2000.0));

            IOHandler.save("users.dat", users);
        }

        // Загрузка товаров
        List<Product> loadedProds = IOHandler.load("products.dat");
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
            String usernameInput = uField.getText();
            String passwordInput = pField.getText();

            // 1. Пробегаемся по списку пользователей
            for (User u : users) {
                // Если логин и пароль совпали
                if (u.getUsername().equals(usernameInput) && u.getPassword().equals(passwordInput)) {

                    // 2. ПРОВЕРКА БЛОКИРОВКИ (Твое требование Access Control)
                    if (u.isBlocked()) {
                        new Alert(Alert.AlertType.ERROR, "ACCESS DENIED. Your account is blocked by Administrator.").show();
                        return; // Останавливаемся, дальше не идем
                    }

                    // 3. Если все ок - пускаем внутрь
                    currentUser = u;
                    showDashboard();
                    return; // ВАЖНО: Выходим из метода, так как нашли человека
                }
            }

            // 4. Если цикл закончился, а мы здесь — значит пароль неверный
            new Alert(Alert.AlertType.ERROR, "Invalid Username or Password!").show();
        });

        root.getChildren().addAll(title, uField, pField, loginBtn);
        stage.setScene(new Scene(root, 400, 350));
        stage.setTitle("Login - Electronics Store");
        stage.show();
    }

    // --- ГЛАВНОЕ МЕНЮ (DASHBOARD) ---
    private void showDashboard() {
        BorderPane root = new BorderPane();

        // --- ЛЕВОЕ МЕНЮ (SIDEBAR) ---
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(200);

        Label welcome = new Label("User: " + currentUser.getUsername());
        welcome.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        sidebar.getChildren().add(welcome);

        // ЦЕНТРАЛЬНАЯ ЧАСТЬ (Меняется при нажатии кнопок)
        StackPane content = new StackPane();
        content.getChildren().add(new Label("Welcome! Select an option from the menu."));

        // КНОПКИ ДЛЯ АДМИНА
        if (currentUser instanceof Admin) {
            Button btnAdmin = createNavButton("Staff & Finance");
            btnAdmin.setOnAction(e -> {
                AdminView view = new AdminView(users, products);
                content.getChildren().setAll(view.getView());
            });
            sidebar.getChildren().add(btnAdmin);
        }

        // КНОПКИ ДЛЯ МЕНЕДЖЕРА (Или Админа)
        if (currentUser instanceof Manager || currentUser instanceof Admin) {
            Button btnManager = createNavButton("Inventory (Manager)");
            btnManager.setOnAction(e -> {
                ManagerView view = new ManagerView(products);
                content.getChildren().setAll(view.getView());
            });
            sidebar.getChildren().add(btnManager);
        }

        // КНОПКИ ДЛЯ КАССИРА (Или Админа)
        if (currentUser instanceof Cashier || currentUser instanceof Admin) {
            Button btnCashier = createNavButton("POS / Sale (Cashier)");
            btnCashier.setOnAction(e -> {
                // Передаем имя текущего кассира для чека
                CashierView view = new CashierView(products, currentUser.getFullName());
                content.getChildren().setAll(view.getView());
            });
            sidebar.getChildren().add(btnCashier);
        }

        // КНОПКА ВЫХОДА
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS); // Толкает кнопку вниз
        Button logout = new Button("Logout");
        logout.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(e -> showLogin());

        sidebar.getChildren().addAll(spacer, logout);

        root.setLeft(sidebar);
        root.setCenter(content);

        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("Dashboard - " + currentUser.getRole());
    }

    // Вспомогательный метод для красоты кнопок
    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-alignment: center-left;");
        return btn;
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
        //
        launch(args);
    }
}