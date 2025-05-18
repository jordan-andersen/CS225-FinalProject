package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import model.ColumnData;
import model.MetadataService;
import model.QueryManager;
import model.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SDSController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button addRowBtn;
    @FXML private TableView<ObservableList<String>> SDSdataTable;
    @FXML private Label statusBar;

    private static final String TABLE_NAME = "SDS";

    private final QueryManager    queries  = new QueryManager();
    private final MetadataService metadata = new MetadataService();
    private final ObservableList<ObservableList<String>> fullData = FXCollections.observableArrayList();

    private User currentUser;

    @FXML
    public void initialize() {
        loadTable(TABLE_NAME);

        searchField.setOnAction(evt -> doSearch());
        searchButton.setOnAction(evt -> doSearch());

        addRowBtn.setDisable(true);
        addRowBtn.setOnAction(evt -> showAddDialog());
    }

    /**
     * Called by DashboardController to pass in the logged-in user.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        boolean admin = isAdmin();
        SDSdataTable.setEditable(admin);
        addRowBtn.setDisable(!admin);
    }

    private boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    private void loadTable(String tableName) {
        try {
            SDSdataTable.getColumns().clear();
            fullData.clear();

            List<ColumnData> colsMeta = metadata.getColumns(tableName);
            List<String> colNames = colsMeta.stream()
                                            .map(ColumnData::getName)
                                            .collect(Collectors.toList());

            List<Map<String, Object>> rows = queries.selectAll(tableName);
            for (Map<String, Object> row : rows) {
                ObservableList<String> obsRow = FXCollections.observableArrayList();
                for (String col : colNames) {
                    obsRow.add(Objects.toString(row.get(col), ""));
                }
                fullData.add(obsRow);
            }

            for (int i = 0; i < colNames.size(); i++) {
                final int idx = i;
                String colName = colNames.get(i);
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colName);
                col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get(idx)));

                if (isAdmin()) {
                    col.setCellFactory(TextFieldTableCell.forTableColumn());
                    col.setOnEditCommit(ev -> {
                        ObservableList<String> editedRow = ev.getRowValue();
                        String oldVal = ev.getOldValue();
                        String newVal = ev.getNewValue();
                        if (!Objects.equals(oldVal, newVal)) {
                            Object pk = editedRow.get(0);
                            queries.updateRow(tableName,
                                              Map.of(colName, newVal),
                                              pk);
                            editedRow.set(idx, newVal);
                            statusBar.setText("Updated " + colName);
                        }
                    });
                }

                SDSdataTable.getColumns().add(col);
            }

            SDSdataTable.setItems(fullData);
            SDSdataTable.setEditable(isAdmin());
            statusBar.setText("Loaded \"" + tableName + "\" (" + fullData.size() + " rows)");
        } catch (Exception e) {
            statusBar.setText("Error loading \"" + tableName + "\": " + e.getMessage());
        }
    }

    private void doSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            SDSdataTable.setItems(fullData);
            statusBar.setText("Showing all rows");
            return;
        }
        FilteredList<ObservableList<String>> filtered = new FilteredList<>(fullData,
            row -> row.stream().anyMatch(cell -> cell.toLowerCase().contains(q))
        );
        SDSdataTable.setItems(filtered);
        statusBar.setText(filtered.size() + " rows match \"" + q + "\"");
    }

    private void showAddDialog() {
        var cols = metadata.getColumns(TABLE_NAME);
        new AddRowDialog(
            SDSdataTable.getScene().getWindow(),
            TABLE_NAME,
            cols,
            values -> {
                queries.insertRow(TABLE_NAME, values);
                loadTable(TABLE_NAME);
            }
        ).showAndWait();
    }
}
