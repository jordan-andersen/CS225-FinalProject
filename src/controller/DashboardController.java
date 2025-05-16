//CHRISTINA KC

package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class DashboardController {

    @FXML private ToggleButton searchBtn, addBtn, sdsBtn, adminBtn;

    private ToggleGroup sidebarGroup;
    private User loggedInUser;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("Logged in user: " + loggedInUser.getName());
    }

    @FXML
    public void initialize() {
        listResourcesDebug();

        System.out.println("searchBtn = " + searchBtn);
        System.out.println("addBtn = " + addBtn);
        System.out.println("sdsBtn = " + sdsBtn);
        System.out.println("adminBtn = " + adminBtn);

        sidebarGroup = new ToggleGroup();

        searchBtn.setToggleGroup(sidebarGroup);
        addBtn.setToggleGroup(sidebarGroup);
        sdsBtn.setToggleGroup(sidebarGroup);
        adminBtn.setToggleGroup(sidebarGroup);

        searchBtn.setSelected(true);  // default selection is inventory

        Platform.runLater(() -> {
            loadCenterModule("inventory.fxml");  //
        });
    }

    @FXML
    private void handleSearch() {
        searchBtn.setSelected(true);
        loadCenterModule("inventory.fxml");  //
    }

    @FXML
    private void handleAdd() {
        addBtn.setSelected(true);
        loadCenterModule("add.fxml");
    }

    @FXML
    private void handleSDS() {
        sdsBtn.setSelected(true);
        loadCenterModule("sds.fxml");
    }

    @FXML
    private void handleAdmin() {
        adminBtn.setSelected(true);
        loadCenterModule("admin.fxml");
    }

    private void loadCenterModule(String fxmlFileName) {
        try {
            String fullPath = "fxml/" + fxmlFileName;
            URL resource = getClass().getClassLoader().getResource(fullPath);
            if (resource == null) {
                throw new IOException("Resource not found: " + fullPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node content = loader.load();

            Parent root = searchBtn.getScene().getRoot();
            if (root instanceof BorderPane mainPane) {
                mainPane.setCenter(content);
            } else {
                System.err.println("Root is not BorderPane!");
            }

            System.out.println("Successfully loaded: " + fxmlFileName);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load module: " + fxmlFileName);
        }
    }

    public void listResourcesDebug() {
        System.out.println("=== Listing resources under fxml/modules/ ===");
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("fxml/modules");
            while (resources.hasMoreElements()) {
                System.out.println("Found: " + resources.nextElement());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

