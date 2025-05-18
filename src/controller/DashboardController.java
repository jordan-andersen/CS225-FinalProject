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

    @FXML private ToggleButton searchBtn;
    @FXML private ToggleButton addBtn;
    @FXML private ToggleButton sdsBtn;
    @FXML private ToggleButton adminBtn;

    private ToggleGroup sidebarGroup;
    private User loggedInUser;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    @FXML
    public void initialize() {
        sidebarGroup = new ToggleGroup();
        searchBtn.setToggleGroup(sidebarGroup);
        addBtn.setToggleGroup(sidebarGroup);
        sdsBtn.setToggleGroup(sidebarGroup);
        adminBtn.setToggleGroup(sidebarGroup);
        searchBtn.setSelected(true);
        Platform.runLater(() -> loadCenterModule("inventory.fxml"));
    }

    @FXML
    private void handleSearch() {
        searchBtn.setSelected(true);
        loadCenterModule("inventory.fxml");
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
            String path = "fxml/" + fxmlFileName;
            URL resource = getClass().getClassLoader().getResource(path);
            if (resource == null) throw new IOException("Resource not found: " + path);
            FXMLLoader loader = new FXMLLoader(resource);
            Node content = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof InventoryController inv) {
                inv.setCurrentUser(loggedInUser);
            }
            Parent root = searchBtn.getScene().getRoot();
            if (root instanceof BorderPane pane) {
                pane.setCenter(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listResourcesDebug() {
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("fxml/modules");
            while (resources.hasMoreElements()) {
                System.out.println(resources.nextElement());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}