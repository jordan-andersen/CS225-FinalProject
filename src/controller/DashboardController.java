package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.application.Platform;
import model.User;

import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML private ToggleButton searchBtn, addBtn, locationBtn, sdsBtn, orderBtn, adminBtn;
    private User loggedInUser;

    @FXML
    public void initialize() {
        System.out.println("DashboardController initialized");
        verifyResources();
        
        ToggleGroup sidebarGroup = new ToggleGroup();
        searchBtn.setToggleGroup(sidebarGroup);
        adminBtn.setToggleGroup(sidebarGroup);
        addBtn.setToggleGroup(sidebarGroup);
        locationBtn.setToggleGroup(sidebarGroup);
        sdsBtn.setToggleGroup(sidebarGroup);
        orderBtn.setToggleGroup(sidebarGroup);

        searchBtn.setSelected(true);
        loadCenterModule("/inventory.fxml");
    }

    private void verifyResources() {
        System.out.println("Verifying resources:");
        System.out.println("Inventory icon: " + getClass().getResource("/images/icons/inventory.png"));
        System.out.println("Inventory FXML: " + getClass().getResource("/inventory.fxml"));
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    private void loadCenterModule(String fxmlPath) {
        try {
            System.out.println("Loading: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            
            if (loader.getController() instanceof InventoryController) {
                ((InventoryController)loader.getController()).setCurrentUser(loggedInUser);
            }
            
            ((BorderPane) searchBtn.getScene().getRoot()).setCenter(content);
        } catch (IOException e) {
            System.err.println("Failed to load: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        loadCenterModule("/inventory.fxml");
    }

    @FXML
    private void handleAdmin() {
        loadCenterModule("/admin.fxml");
    }

    @FXML
    private void handleAdd() {
        loadCenterModule("/add.fxml");
    }

    @FXML
    private void handleLocation() {
        loadCenterModule("/location.fxml");
    }

    @FXML
    private void handleSDS() {
        loadCenterModule("/sds.fxml");
    }

    @FXML
    private void handleOrder() {
        loadCenterModule("/order.fxml");
    }
}
