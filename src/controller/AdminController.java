package controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.QueryManager;
import model.UserManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AdminController handles the Users administration view.
 */
public class AdminController {

    @FXML private TableView<Map<String, Object>> usersTable;          // NEW: parent TableView
    @FXML private TableColumn<Map<String, Object>, String> userTable; // username column
    @FXML private TableColumn<Map<String, Object>, String> roleTable; // role column

    @FXML private Button addBtn;
    @FXML private Button removeBtn;

    private TableView<Map<String, Object>> tv;   // reference used in handlers

    private final UserManager userManager = new UserManager();
    private final QueryManager queryManager = new QueryManager();

    @FXML
    public void initialize() {
        /* Reference to the parent TableView once FXML is loaded */
        tv = (usersTable != null) ? usersTable : userTable.getTableView();

        /* Column cell value factories */
        userTable.setCellValueFactory(cell -> {
            Object v = cell.getValue().get("username");
            return new ReadOnlyStringWrapper(v == null ? "" : v.toString());
        });

        roleTable.setCellValueFactory(cell -> {
            Object v = cell.getValue().get("role");
            return new ReadOnlyStringWrapper(v == null ? "" : v.toString());
        });

        /* Button actions */
        addBtn.setOnAction(e -> addUser());
        removeBtn.setOnAction(e -> removeUser());

        /* Initial load */
        refreshTable();
    }

    private void addUser() {
        /* Prompt for username */
        TextInputDialog userDlg = new TextInputDialog();
        userDlg.setTitle("Add User");
        userDlg.setHeaderText("Enter new username:");
        Optional<String> uname = userDlg.showAndWait();

        if (uname.isEmpty() || uname.get().trim().isEmpty()) {
            return;
        }

        /* Prompt for role */
        ChoiceDialog<String> roleDlg = new ChoiceDialog<>("user", List.of("admin", "user", "guest"));
        roleDlg.setTitle("Select Role");
        roleDlg.setHeaderText("Role for " + uname.get());
        Optional<String> role = roleDlg.showAndWait();

        if (role.isEmpty()) {
            return;
        }

        /* Create user and refresh table */
        userManager.createUser(uname.get().trim(), "password123", role.get());
        refreshTable();
    }

    private void removeUser() {
        Map<String, Object> row = tv.getSelectionModel().getSelectedItem();
        if (row != null) {
            String username = (String) row.get("username");
            userManager.deleteUser(username);
            refreshTable();
        }
    }

    private void refreshTable() {
        List<Map<String, Object>> rows = queryManager.selectAll("Users");
        tv.getItems().setAll(rows);
    }
}
