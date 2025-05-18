package controller;

/**
 * InventoryController manages the behavior of the Inventory view in the chemical inventory system.
 * Author: Daniel and Anna.
 */

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import model.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class InventoryController {

    @FXML private ListView<String> categoriesList;
    @FXML private TableView<ObservableList<String>> dataTable;
    @FXML private TextField searchField;
    @FXML private Label statusBar;
    @FXML private ToggleButton toggleBtn;
    @FXML private Button searchButton;
    @FXML private Button casBttn;
    @FXML private Button adminBttn;
    @FXML private Button tableBttn;
    @FXML private Button addRowBtn;

    private User currentUser;
    private String currentTableName;
    private final ObservableList<ObservableList<String>> fullData = FXCollections.observableArrayList();

    private final MetadataService metadata = new MetadataService();
    private final QueryManager    queries  = new QueryManager();

    @FXML
    public void initialize() {

        try {
            List<String> tables = metadata.listTables();
            if (tables != null) categoriesList.setItems(FXCollections.observableArrayList(tables));
        } catch (Exception e) {
            statusBar.setText("Error loading table list: " + e.getMessage());
        }

        if (tableBttn != null && categoriesList != null) {
            tableBttn.setOnAction(evt -> {
                boolean visible = categoriesList.isVisible();
                categoriesList.setVisible(!visible);
                categoriesList.setManaged(!visible);
            });
        }

        categoriesList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                searchField.clear();
                loadTable(n);
            }
            if (addRowBtn != null) addRowBtn.setDisable(n == null || !isAdmin());
        });

        searchField.setOnAction(evt -> doSearch());
        searchButton.setOnAction(evt -> doSearch());
        if (toggleBtn != null) toggleBtn.setOnAction(evt -> {
            if (!searchField.getText().trim().isEmpty()) doSearch();
        });

        if (casBttn != null) casBttn.setOnAction(evt -> promptCas());

        if (addRowBtn != null) {
            addRowBtn.setDisable(true);
            addRowBtn.setOnAction(evt -> showAddDialog());
        }

        applyRolePermissions();
    }

    private void loadTable(String tableName) {
        currentTableName = tableName;
        try {
            dataTable.getColumns().clear();
            fullData.clear();

            List<ColumnData> meta  = metadata.getColumns(tableName);
            List<String>     names = meta.stream().map(ColumnData::getName).collect(Collectors.toList());

            List<Map<String,Object>> rows = queries.selectAll(tableName);
            for (Map<String,Object> m : rows) {
                ObservableList<String> r = FXCollections.observableArrayList();
                for (String c : names) r.add(Objects.toString(m.get(c), ""));
                fullData.add(r);
            }

            for (int i = 0; i < names.size(); i++) {
                final int idx = i;
                final String colName = names.get(i);
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colName);
                col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get(idx)));

                ColumnData cMeta = meta.get(i);
                if (cMeta.isPrimaryKey() && "COUNTER".equalsIgnoreCase(cMeta.getType())) {
                    col.setVisible(false);
                    col.setPrefWidth(0);
                }

                if (isAdmin()) {
                    col.setEditable(true);
                    col.setCellFactory(TextFieldTableCell.forTableColumn());
                    col.setOnEditCommit(ev -> {
                        ObservableList<String> row = ev.getRowValue();
                        if (!ev.getNewValue().equals(ev.getOldValue())) {
                            queries.updateRow(tableName, Map.of(colName, ev.getNewValue()), row.get(0));
                            row.set(idx, ev.getNewValue());
                            statusBar.setText("Updated " + colName);
                        }
                    });
                }
                dataTable.getColumns().add(col);
            }

            dataTable.setItems(fullData);
            dataTable.setEditable(isAdmin());
            statusBar.setText("Loaded \"" + tableName + "\" (" + fullData.size() + " rows)");

        } catch (Exception e) {
            statusBar.setText("Error loading table \"" + tableName + "\": " + e.getMessage());
        }
    }

    private void doSearch() {
        if (currentTableName == null) return;
        String q = searchField.getText().trim();
        if (q.isEmpty()) {
            dataTable.setItems(fullData);
            statusBar.setText("Showing all rows");
            return;
        }

        try {
            if (toggleBtn != null && toggleBtn.isSelected()) {
                FilteredList<ObservableList<String>> fl = new FilteredList<>(fullData,
                        r -> r.stream().anyMatch(c -> c.toLowerCase().contains(q.toLowerCase())));
                dataTable.setItems(fl);
                statusBar.setText(fl.size() + " rows match \"" + q + "\"");
            } else {
                List<Map<String,Object>> matches = queries.search(currentTableName, q);
                List<String> cols = metadata.getColumns(currentTableName).stream()
                        .map(ColumnData::getName).collect(Collectors.toList());
                ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
                for (Map<String,Object> m : matches) {
                    ObservableList<String> r = FXCollections.observableArrayList();
                    for (String c : cols) r.add(Objects.toString(m.get(c), ""));
                    data.add(r);
                }
                dataTable.setItems(data);
                statusBar.setText(data.size() + " rows found");
            }
        } catch (Exception e) {
            statusBar.setText("Search error: " + e.getMessage());
        }
    }

    private void promptCas() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Lookup by CAS");
        d.setHeaderText(null);
        d.setContentText("CAS Number:");
        d.showAndWait().ifPresent(cas -> {
            if (!cas.trim().isEmpty()) {
                try { new PubChemService().browseByCas(cas.trim()); }
                catch (Exception ex) { statusBar.setText("CAS lookup failed"); }
            }
        });
    }

    private void applyRolePermissions() {
        if (adminBttn != null) {
            adminBttn.setVisible(isAdmin());
            adminBttn.setManaged(isAdmin());
        }
        if (dataTable != null) dataTable.setEditable(isAdmin());
        if (addRowBtn != null)  addRowBtn.setDisable(!isAdmin());
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        applyRolePermissions();
    }

    private boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    private void showAddDialog() {
        if (currentTableName == null) return;
        List<ColumnData> cols = metadata.getColumns(currentTableName);
        new AddRowDialog(dataTable.getScene().getWindow(), currentTableName, cols, v -> {
            queries.insertRow(currentTableName, v);
            loadTable(currentTableName);
        }).showAndWait();
    }
}