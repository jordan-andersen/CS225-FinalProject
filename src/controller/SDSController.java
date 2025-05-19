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

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Lucas L., Abraham A., Jordan A.
 */
public class SDSController {
    @FXML private TextField fileSearchField;
    @FXML private Button fileSearchButton;
    @FXML private TableView<ObservableList<String>> SDSdataTable;
    @FXML private Label statusBar;
    @FXML private Button addRowBtn;

    @FXML private TextField     dataSearchField;
    @FXML private Button        dataSearchButton;
    @FXML private ListView<Path> sdsList;
    @FXML private Label dataStatusBar;


    private final QueryManager queries      = new QueryManager();
    private final MetadataService metadata   = new MetadataService();
    private final ObservableList<ObservableList<String>> fullData = FXCollections.observableArrayList();

    private static final String TABLE_NAME = "SDS";
    private static final Path SDS_DIR = Paths.get("data", "SDS");
    private final ObservableList<Path> fullList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load the initial SDS table data
        loadTable();

        // Wire up search actions
        dataSearchField.setOnAction(evt -> doDataSearch());
        dataSearchButton.setOnAction(evt -> doDataSearch());
        addRowBtn.setOnAction(evt -> showAddDialog());

        loadDirectory();

        fileSearchField.setOnAction(e -> doFileSearch());
        fileSearchButton.setOnAction(e -> doFileSearch());

        sdsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Path p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getFileName().toString());
            }
        });

        sdsList.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2 && sdsList.getSelectionModel().getSelectedItem() != null) {
                openFileAsync(sdsList.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void loadDirectory() {
        fullList.clear();

        if (!Files.isDirectory(SDS_DIR)) {
            dataStatusBar.setText("Directory not found: " + SDS_DIR.toAbsolutePath());
            return;
        }
        try (var stream = Files.list(SDS_DIR)) {
            fullList.addAll(stream
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .collect(Collectors.toList()));
        } catch (IOException ex) {
            dataStatusBar.setText("Error reading directory: " + ex.getMessage());
            return;
        }

        sdsList.setItems(fullList);
        dataStatusBar.setText(fullList.size() + " files");
    }

    private void doFileSearch() {
        String q = fileSearchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            sdsList.setItems(fullList);
            dataStatusBar.setText("Showing all files");
            return;
        }
        FilteredList<Path> filtered =
                new FilteredList<>(fullList,
                        p -> p.getFileName().toString().toLowerCase().contains(q));

        sdsList.setItems(filtered);
        dataStatusBar.setText(filtered.size() + " files match \"" + q + "\"");
    }

    private void openFileAsync(Path file) {
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file.toFile());
                } else {
                    showInfo("This platform can’t auto-open files.\n\nPath:\n" +
                             file.toAbsolutePath());
                }
            } catch (Exception ex) {
                showInfo("Couldn’t open the file:\n" + ex.getMessage() +
                         "\n\nPath:\n" + file.toAbsolutePath());
            }
        }, "file-opener").start();
    }

    private void showInfo(String msg) {
        javafx.application.Platform.runLater(() ->
                new Alert(Alert.AlertType.INFORMATION, msg).showAndWait());
    }

    private void loadTable() {
        try {
            SDSdataTable.getColumns().clear();
            fullData.clear();

            // Retrieve column metadata
            List<ColumnData> colsMeta = metadata.getColumns(TABLE_NAME);
            List<String> colNames = colsMeta.stream()
                    .map(ColumnData::getName)
                    .collect(Collectors.toList());

            // Load all rows
            List<Map<String, Object>> rows = queries.selectAll(TABLE_NAME);
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
                        queries.updateRow(TABLE_NAME,
                                Map.of(colName, newVal),
                                pk);
                        editedRow.set(idx, newVal);
                        dataStatusBar.setText("Updated " + colName);
                    }
                });

                SDSdataTable.getColumns().add(col);
            }

            SDSdataTable.setItems(fullData);
            SDSdataTable.setEditable(true);
            dataStatusBar.setText("Loaded \"" + TABLE_NAME + "\" (" + fullData.size() + " rows)");
        } catch (Exception e) {
            dataStatusBar.setText("Error loading table \"" + TABLE_NAME + "\": " + e.getMessage());
        }
    }

    private void doDataSearch() {
        String q = dataSearchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            SDSdataTable.setItems(fullData);
            dataStatusBar.setText("Showing all rows");
            return;
        }

        FilteredList<ObservableList<String>> filtered =
                new FilteredList<>(fullData, row ->
                        row.stream().anyMatch(cell -> cell.toLowerCase().contains(q))
                );

        SDSdataTable.setItems(filtered);
        dataStatusBar.setText(filtered.size() + " rows match \"" + q + "\"");
    }

    private void showAddDialog() {
        List<ColumnData> cols = metadata.getColumns(TABLE_NAME);
        new AddRowDialog(SDSdataTable.getScene().getWindow(), TABLE_NAME, cols, v -> {
            queries.insertRow(TABLE_NAME, v);
            loadTable();
        }).showAndWait();
    }
}
