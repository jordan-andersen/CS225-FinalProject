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
        TextInputDialog userDlg = new TextInputDialog();
        userDlg.setTitle("Add User");
        userDlg.setHeaderText("Enter new username:");
        Optional<String> uname = userDlg.showAndWait();
        if (uname.isEmpty() || uname.get().trim().isEmpty()){
            return;
        }

        // prompt for role
        ChoiceDialog<String> roleDlg = new ChoiceDialog<>("user", List.of("admin","user","guest"));
        roleDlg.setTitle("Select Role");
        roleDlg.setHeaderText("Role for " + uname.get());
        Optional<String> role = roleDlg.showAndWait();
        if (role.isEmpty()){
            return;
        }

        // create user and refresh
        userManager.createUser(uname.get().trim(), "password123", role.get());
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
