package com.store.view;

import com.store.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DashboardView {
    private BorderPane root;
    private VBox sidebar;
    private Button logoutBtn;
    private StackPane contentArea;

    // Храним кнопки, чтобы Контроллер мог повесить на них действия
    private Map<String, Button> navButtons = new HashMap<>();

    public DashboardView(User user) {
        root = new BorderPane();

        sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(220);

        // --- ЛОГОТИП (ВСТАВКА) ---
        ImageView menuLogo = new ImageView();
        try {
            // Убедись, что импорты добавлены: import javafx.scene.image.*;
            Image img = new Image(getClass().getResourceAsStream("/com/store/electronicstoreapp/img.png"));
            menuLogo.setImage(img);
            menuLogo.setFitWidth(100);
            menuLogo.setPreserveRatio(true);

            HBox logoContainer = new HBox(menuLogo);
            logoContainer.setAlignment(javafx.geometry.Pos.CENTER);
            logoContainer.setPadding(new Insets(0, 0, 10, 0));

            sidebar.getChildren().add(logoContainer);
        } catch (Exception e) {
            // Игнорируем ошибку, если картинки нет
        }
        // -------------------------

        // ИСПРАВЛЕНИЕ ОШИБКИ ЗДЕСЬ:
        Label welcome = new Label("Welcome,\n" + user.getFullName()); // Используем user.getFullName()
        welcome.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label roleLabel = new Label("[" + user.getRole() + "]"); // Используем user.getRole()
        roleLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 12px;");

        sidebar.getChildren().addAll(welcome, roleLabel, new Separator());

        // --- ДАЛЕЕ КОД КНОПОК ОСТАЕТСЯ ТЕМ ЖЕ ---
        String r = user.getRole();

        if (r.equals("Administrator")) {
            createNavButton("Manage Staff");
        }

        if (r.equals("Manager") || r.equals("Administrator")) {
            createNavButton("Inventory Management");
        }

        if (r.equals("Cashier") || r.equals("Administrator")) {
            createNavButton("New Sale (POS)");
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-cursor: hand;");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);

        sidebar.getChildren().addAll(spacer, logoutBtn);

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.getChildren().add(new Label("Select an option from the sidebar to begin."));

        root.setLeft(sidebar);
        root.setCenter(contentArea);
    }

    private void createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-alignment: center-left; -fx-padding: 10; -fx-cursor: hand;");

        // Эффект при наведении (опционально)
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-alignment: center-left; -fx-padding: 10;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-alignment: center-left; -fx-padding: 10;"));

        sidebar.getChildren().add(btn);
        navButtons.put(text, btn);
    }

    public BorderPane getRoot() { return root; }
    public Button getLogoutBtn() { return logoutBtn; }
    public Button getNavButton(String name) { return navButtons.get(name); }

    // Метод для смены экрана в центре
    public void setCenter(javafx.scene.Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }
}