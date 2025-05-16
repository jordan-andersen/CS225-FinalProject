package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import model.UserManager;
import model.QueryManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdminController {
    @FXML private TableColumn<Map<String,Object>,String> userTable;
    @FXML private TableColumn<Map<String,Object>,String> roleTable;

    @FXML private Button addBtn;
    @FXML private Button removeBtn;

    private TableView<Map<String,Object>> tv;

    private final UserManager  userManager  = new UserManager();
    private final QueryManager queryManager = new QueryManager();

    @FXML
    public void initialize() {
        tv = userTable.getTableView();

        userTable.setCellValueFactory(cell -> {Object v = cell.getValue().get("username");
            return new ReadOnlyStringWrapper(v == null ? "" : v.toString());
        });
        roleTable.setCellValueFactory(cell -> {Object v = cell.getValue().get("role");
            return new ReadOnlyStringWrapper(v == null ? "" : v.toString());
        });

        addBtn.setOnAction(e -> addUser());
        removeBtn.setOnAction(e -> removeUser());

        refreshTable();
    }

    private void addUser() {
        // prompt for username
        TextInputDialog userDialog = new TextInputDialog();
        userDialog.setTitle("Add User");
        userDialog.setHeaderText("Enter new username:");
        Optional<String> username = userDialog.showAndWait();
        if (username.isEmpty() || username.get().trim().isEmpty()){
            return;
        }

        // prompt for role
        ChoiceDialog<String> roleDialog = new ChoiceDialog<>("user", List.of("admin","user","guest"));
        roleDialog.setTitle("Select Role");
        roleDialog.setHeaderText("Role for " + username.get());
        Optional<String> role = roleDialog.showAndWait();
        if (role.isEmpty()){
            return;
        }

        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Add User");
        passwordDialog.setHeaderText("Enter new username:");
        Optional<String> password = passwordDialog.showAndWait();
        if (password.isEmpty() || password.get().trim().isEmpty()){
            return;
        }
        // create user and refresh
        userManager.createUser(username.get().trim(), password.get().trim(), role.get());
        refreshTable();
    }

    private void removeUser() {
        Map<String,Object> row = tv.getSelectionModel().getSelectedItem();
        if (row != null) {
            String username = (String) row.get("username");
            userManager.deleteUser(username);
            refreshTable();
        }
    }

    private void refreshTable() {
        List<Map<String,Object>> rows = queryManager.selectAll("Users");
        tv.getItems().setAll(rows);
    }
}
