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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private User currentUser;
    private String currentTableName;
    private final ObservableList<ObservableList<String>> fullData = FXCollections.observableArrayList();

    private final MetadataService metadata = new MetadataService();
    private final QueryManager  queries  = new QueryManager();

    @FXML
    public void initialize() {
        try {
            List<String> tables = metadata.listTables();
            if (tables != null) {
                categoriesList.setItems(FXCollections.observableArrayList(tables));
            }
        } catch (Exception e) {
            statusBar.setText("Error loading table list: " + e.getMessage());
        }

        categoriesList.getSelectionModel().selectedItemProperty().addListener((obs, oldTable, newTable) -> {
            if (newTable != null) {
                searchField.clear();
                loadTable(newTable);
            }
        });

        searchField.setOnAction(evt -> doSearch());
        searchButton.setOnAction(evt -> doSearch());
        if (toggleBtn != null) {
            toggleBtn.setOnAction(evt -> {
                if (!searchField.getText().trim().isEmpty()) doSearch();
            });
        }

        if (casBttn != null) {
            casBttn.setOnAction(evt -> promptCas());
        }

        applyRolePermissions();
    }

    private void loadTable(String tableName) {
        currentTableName = tableName;
        try {
            dataTable.getColumns().clear();
            fullData.clear();

            List<ColumnData> columnDataList = metadata.getColumns(tableName);
            List<String> columnNames = columnDataList.stream().map(ColumnData::getName).collect(Collectors.toList());

            List<Map<String, Object>> rowMaps = queries.selectAll(tableName);
            for (Map<String, Object> map : rowMaps) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (String colName : columnNames) {
                    Object val = map.get(colName);
                    row.add(val == null ? "" : val.toString());
                }
                fullData.add(row);
            }

            for (int i = 0; i < columnNames.size(); i++) {
                final int colIndex = i;
                final String colName = columnNames.get(i);
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colName);
                col.setCellValueFactory(cellData -> {
                    ObservableList<String> rowValues = cellData.getValue();
                    String value = (rowValues != null && colIndex < rowValues.size()) ? rowValues.get(colIndex) : "";
                    return new SimpleStringProperty(value);
                });

                boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
                if (isAdmin) {
                    col.setEditable(true);
                    col.setCellFactory(TextFieldTableCell.forTableColumn());
                    col.setOnEditCommit(evt -> {
                        ObservableList<String> rowData = evt.getRowValue();
                        String newValue = evt.getNewValue();
                        String oldValue = evt.getOldValue();
                        if (!newValue.equals(oldValue)) {
                            String idValue = rowData.get(0);
                            queries.updateRow(tableName, Map.of(colName, newValue), idValue);
                            rowData.set(colIndex, newValue);
                            statusBar.setText("Updated " + colName + " for ID " + idValue);
                        }
                    });
                }
                dataTable.getColumns().add(col);
            }

            dataTable.setItems(fullData);
            dataTable.setEditable(currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole()));

            /* row double-click opens details dialog */
            dataTable.setRowFactory(tv -> {
                TableRow<ObservableList<String>> row = new TableRow<>();
                row.setOnMouseClicked(evt -> {
                    if (evt.getClickCount() == 2 && !row.isEmpty()) {
                        ObservableList<String> vals = row.getItem();
                        Map<String,Object> map = new LinkedHashMap<>();
                        for (int i = 0; i < dataTable.getColumns().size(); i++) {
                            String colName = dataTable.getColumns().get(i).getText();
                            map.put(colName, vals.get(i));
                        }
                        new RowDetailsDialog(dataTable.getScene().getWindow(), currentTableName, map).showAndWait();
                    }
                });
                return row;
            });

            statusBar.setText("Loaded table \"" + tableName + "\" (" + fullData.size() + " rows)");
        } catch (Exception e) {
            statusBar.setText("Error loading table \"" + tableName + "\": " + e.getMessage());
        }
    }

    private void doSearch() {
        if (currentTableName == null || currentTableName.isEmpty()) return;

        String queryText = searchField.getText().trim();
        if (queryText.isEmpty()) {
            dataTable.setItems(fullData);
            statusBar.setText("Showing all rows for \"" + currentTableName + "\"");
            return;
        }

        try {
            boolean useClientFilter = toggleBtn != null && toggleBtn.isSelected();
            if (useClientFilter) {
                FilteredList<ObservableList<String>> filtered = new FilteredList<>(fullData, row ->
                        row.stream().anyMatch(cell -> cell != null && cell.toLowerCase().contains(queryText.toLowerCase())));
                dataTable.setItems(filtered);
                statusBar.setText("Filtered results: " + filtered.size() + " rows match \"" + queryText + "\"");
            } else {
                List<Map<String, Object>> matches = queries.search(currentTableName, queryText);
                List<String> colNames = metadata.getColumns(currentTableName).stream().map(ColumnData::getName).toList();

                ObservableList<ObservableList<String>> searchData = FXCollections.observableArrayList();
                for (Map<String, Object> map : matches) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (String col : colNames) {
                        Object val = map.get(col);
                        row.add(val == null ? "" : val.toString());
                    }
                    searchData.add(row);
                }
                dataTable.setItems(searchData);
                statusBar.setText("Search results: " + searchData.size() + " rows found for \"" + queryText + "\"");
            }
        } catch (Exception e) {
            statusBar.setText("Search error: " + e.getMessage());
        }
    }

    private void promptCas() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Lookup by CAS");
        dialog.setHeaderText("Enter a CAS Number to search in PubChem");
        dialog.setContentText("CAS Number:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(cas -> {
            String casNo = cas.trim();
            if (!casNo.isEmpty()) {
                try {
                    new PubChemService().browseByCas(casNo);
                    statusBar.setText("Opened PubChem search for CAS " + casNo);
                } catch (Exception e) {
                    statusBar.setText("Failed to lookup CAS " + casNo + ": " + e.getMessage());
                }
            }
        });
    }

    private void applyRolePermissions() {
        boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
        if (adminBttn != null) {
            adminBttn.setVisible(isAdmin);
            adminBttn.setManaged(isAdmin);
        }
        if (dataTable != null) {
            dataTable.setEditable(isAdmin);
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        applyRolePermissions();
    }
}
