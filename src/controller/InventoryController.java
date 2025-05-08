package controller;

/**
 * InventoryController manages the behavior of the Inventory view in the chemical inventory system.
 * Author: Daniel and Anna
 */

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import model.User;
import model.QueryManager;
import model.MetadataService;
import model.ColumnData;
import model.PubChemService;

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
    @FXML private Button searchButton, casBttn, adminBttn, tableBttn;



    private User currentUser;
    private String currentTableName;
    private final ObservableList<ObservableList<String>> fullData = FXCollections.observableArrayList();
    private final MetadataService metadata = new MetadataService();
    private final QueryManager queries = new QueryManager();

    @FXML
    public void initialize() {
        try {
            List<String> tables = metadata.listTables();
            if (tables != null) {
                tableList.setItems(FXCollections.observableArrayList(tables));
            }
        } catch (Exception e) {
            statusBar.setText("Error loading table list: " + e.getMessage());
        }

        tableList.getSelectionModel().selectedItemProperty().addListener((obs, oldTable, newTable) -> {
            if (newTable != null) {
                searchField.clear();
                loadTable(newTable);
            }
        });

        searchField.setOnAction(event -> doSearch());
        goBtn.setOnAction(event -> doSearch());
        toggleBtn.setOnAction(event -> {
            if (!searchField.getText().trim().isEmpty()) doSearch();
        });

        applyRolePermissions();
    }

    private void loadTable(String tableName) {
        currentTableName = tableName;
        try {
            dataTable.getColumns().clear();
            fullData.clear();

            List<ColumnData> columnDataList = metadata.getColumns(tableName);
            List<String> columnNames = columnDataList.stream()
                    .map(ColumnData::getName)
                    .collect(Collectors.toList());

            List<Map<String, Object>> rowMaps = queries.selectAll(tableName);
            List<List<String>> rows = rowMaps.stream()
                    .map(map -> columnNames.stream()
                            .map(name -> {
                                Object val = map.get(name);
                                return val == null ? "" : val.toString();
                            })
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            for (List<String> row : rows) {
                fullData.add(FXCollections.observableArrayList(row));
            }

            for (int i = 0; i < columnNames.size(); i++) {
                final int colIndex = i;
                final String colName = columnNames.get(i);
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colName);

                col.setCellValueFactory(cellData -> {
                    ObservableList<String> rowValues = cellData.getValue();
                    String cellValue = (rowValues != null && colIndex < rowValues.size()) ? rowValues.get(colIndex) : "";
                    return new SimpleStringProperty(cellValue);
                });

                if (currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole())) {
                    col.setEditable(true);
                    col.setCellFactory(TextFieldTableCell.forTableColumn());
                    col.setOnEditCommit(event -> {
                        ObservableList<String> rowData = event.getRowValue();
                        String newValue = event.getNewValue();
                        String oldValue = event.getOldValue();
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
            statusBar.setText("Loaded table \"" + tableName + "\" (" + fullData.size() + " rows)");
        } catch (Exception e) {
            statusBar.setText("Error loading table \"" + tableName + "\": " + e.getMessage());
        }
    }

    @FXML
    private void doSearch() {
        if (currentTableName == null || currentTableName.isEmpty()) return;

        String queryText = searchField.getText().trim();
        if (queryText.isEmpty()) {
            dataTable.setItems(fullData);
            statusBar.setText("Showing all rows for \"" + currentTableName + "\"");
            return;
        }

        try {
            if (toggleBtn.isSelected()) {
                FilteredList<ObservableList<String>> filteredData = new FilteredList<>(fullData, row ->
                        row.stream().anyMatch(cell -> cell != null && cell.toLowerCase().contains(queryText.toLowerCase())));
                dataTable.setItems(filteredData);
                statusBar.setText("Filtered results: " + filteredData.size() + " rows match \"" + queryText + "\"");
            } else {
                List<Map<String, Object>> resultMaps = queries.search(currentTableName, queryText);
                List<ColumnData> columnDataList = metadata.getColumns(currentTableName);
                List<String> columnNames = columnDataList.stream().map(ColumnData::getName).toList();

                ObservableList<ObservableList<String>> searchData = FXCollections.observableArrayList();
                for (Map<String, Object> map : resultMaps) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (String col : columnNames) {
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

    @FXML
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
        boolean isAdmin = (currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole()));
        if (usersBtn != null) {
            usersBtn.setVisible(isAdmin);
            usersBtn.setManaged(isAdmin);
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