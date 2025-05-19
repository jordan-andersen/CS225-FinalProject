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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author Lucas L., Abraham A., Jordan A.
 */
public class SDSController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private TableView<ObservableList<String>> SDSdataTable;
    @FXML private Label statusBar;
    @FXML private Button addRowBtn;

    private final QueryManager queries      = new QueryManager();
    private final MetadataService metadata   = new MetadataService();
    private final ObservableList<ObservableList<String>> fullData = FXCollections.observableArrayList();

    // Name of the table in the database for SDS data
    private static final String TABLE_NAME = "SDS";

    @FXML
    public void initialize() {
        // Load the initial SDS table data
        loadTable(TABLE_NAME);

        // Wire up search actions
        searchField.setOnAction(evt -> doSearch());
        searchButton.setOnAction(evt -> doSearch());
        addRowBtn.setOnAction(evt -> showAddDialog());
    }

    private void loadTable(String tableName) {
        try {
            SDSdataTable.getColumns().clear();
            fullData.clear();

            // Retrieve column metadata
            List<ColumnData> colsMeta = metadata.getColumns(tableName);
            List<String> colNames = colsMeta.stream()
                                           .map(ColumnData::getName)
                                           .collect(Collectors.toList());

            // Load all rows
            List<Map<String, Object>> rows = queries.selectAll(tableName);
            for (Map<String, Object> row : rows) {
                ObservableList<String> obsRow = FXCollections.observableArrayList();
                for (String col : colNames) {
                    obsRow.add(Objects.toString(row.get(col), ""));
                }
                fullData.add(obsRow);
            }

            // Create columns dynamically
            for (int i = 0; i < colNames.size(); i++) {
                final int idx = i;
                String colName = colNames.get(i);

                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colName);
                col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get(idx)));

                // Allow editing if needed
                col.setCellFactory(TextFieldTableCell.forTableColumn());
                col.setOnEditCommit(ev -> {
                    ObservableList<String> editedRow = ev.getRowValue();
                    String oldVal = ev.getOldValue();
                    String newVal = ev.getNewValue();
                    if (!Objects.equals(oldVal, newVal)) {
                        // Update DB using first column (assumed PK) as identifier
                        Object pk = editedRow.get(0);
                        queries.updateRow(tableName,
                                          Map.of(colName, newVal),
                                          pk);
                        editedRow.set(idx, newVal);
                        statusBar.setText("Updated " + colName);
                    }
                });

                SDSdataTable.getColumns().add(col);
            }

            SDSdataTable.setItems(fullData);
            SDSdataTable.setEditable(true);
            statusBar.setText("Loaded \"" + tableName + "\" (" + fullData.size() + " rows)");
        } catch (Exception e) {
            statusBar.setText("Error loading table \"" + tableName + "\": " + e.getMessage());
        }
    }

    private void doSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            SDSdataTable.setItems(fullData);
            statusBar.setText("Showing all rows");
            return;
        }

        FilteredList<ObservableList<String>> filtered =
            new FilteredList<>(fullData, row ->
                row.stream().anyMatch(cell -> cell.toLowerCase().contains(q))
            );

        SDSdataTable.setItems(filtered);
        statusBar.setText(filtered.size() + " rows match \"" + q + "\"");
    }

    private void showAddDialog() {
        List<ColumnData> cols = metadata.getColumns(TABLE_NAME);
        new AddRowDialog(SDSdataTable.getScene().getWindow(), TABLE_NAME, cols, v -> {
            queries.insertRow(TABLE_NAME, v);
            loadTable(TABLE_NAME);
        }).showAndWait();
    }
}
