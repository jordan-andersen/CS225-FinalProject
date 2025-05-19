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
 *
 * @author Lucas L., Abraham A., Jordan A.
 */
public class SDSController {

    /* table widgets */
    @FXML private TextField dataSearchField;
    @FXML private Button    dataSearchButton;
    @FXML private TableView<ObservableList<String>> SDSdataTable;
    @FXML private Button    addRowBtn;
    @FXML private Label     dataStatusBar;

    /* file-browser widgets */
    @FXML private TextField fileSearchField;
    @FXML private Button    fileSearchButton;
    @FXML private ListView<Path> sdsList;

    /* toggle button declared in FXML */
    @FXML private Button tableBttn;

    /* constants / state */
    private static final String TABLE_NAME = "SDS";
    private static final Path   SDS_DIR    = Paths.get("data", "SDS");

    private final QueryManager    queries   = new QueryManager();
    private final MetadataService metadata  = new MetadataService();

    private final ObservableList<ObservableList<String>> fullData  = FXCollections.observableArrayList();
    private final ObservableList<Path>                   fullFiles = FXCollections.observableArrayList();

    /* life-cycle */
    @FXML
    public void initialize() {

        /* build both panes */
        loadTable();
        loadDirectory();

        /* table search / add */
        dataSearchField.setOnAction(e -> doDataSearch());
        dataSearchButton.setOnAction(e -> doDataSearch());
        addRowBtn.setOnAction(e -> showAddDialog());

        /* file search + open */
        fileSearchField.setOnAction(e -> doFileSearch());
        fileSearchButton.setOnAction(e -> doFileSearch());

        sdsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Path p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getFileName().toString());
            }
        });
        sdsList.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2 && sdsList.getSelectionModel().getSelectedItem() != null)
                openFileAsync(sdsList.getSelectionModel().getSelectedItem());
        });

        /* optional: toggle which pane is visible */
        if (tableBttn != null) {
            tableBttn.setOnAction(e -> togglePanes());
        }
    }

    /* directory (right pane) */
    private void loadDirectory() {
        fullFiles.clear();

        if (!Files.isDirectory(SDS_DIR)) {
            dataStatusBar.setText("Directory not found: " + SDS_DIR.toAbsolutePath());
            return;
        }
        try (var stream = Files.list(SDS_DIR)) {
            fullFiles.addAll(stream
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .collect(Collectors.toList()));
        } catch (IOException ex) {
            dataStatusBar.setText("Error reading directory: " + ex.getMessage());
            return;
        }
        sdsList.setItems(fullFiles);
        dataStatusBar.setText(fullFiles.size() + " files");
    }

    private void doFileSearch() {
        String q = fileSearchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            sdsList.setItems(fullFiles);
            dataStatusBar.setText("Showing all files");
            return;
        }
        FilteredList<Path> filtered =
                new FilteredList<>(fullFiles, p -> p.getFileName().toString().toLowerCase().contains(q));

        sdsList.setItems(filtered);
        dataStatusBar.setText(filtered.size() + " files match \"" + q + "\"");
    }

    private void openFileAsync(Path file) {
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file.toFile());
                } else {
                    showInfo("This platform can’t auto-open files.\n\n" + file.toAbsolutePath());
                }
            } catch (Exception ex) {
                showInfo("Couldn’t open file:\n" + ex.getMessage() + "\n\n" + file.toAbsolutePath());
            }
        }, "file-opener").start();
    }

    /* database table (left pane) */
    private void loadTable() {
        try {
            SDSdataTable.getColumns().clear();
            fullData.clear();

            List<ColumnData> colsMeta = metadata.getColumns(TABLE_NAME);
            List<String>     colNames = colsMeta.stream().map(ColumnData::getName).toList();

            /* rows */
            for (Map<String,Object> row : queries.selectAll(TABLE_NAME)) {
                ObservableList<String> obs = FXCollections.observableArrayList();
                for (String c : colNames) obs.add(Objects.toString(row.get(c), ""));
                fullData.add(obs);
            }

            /* columns */
            for (int i = 0; i < colNames.size(); i++) {
                final int idx = i;
                final String colName = colNames.get(i);

                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colName);
                col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get(idx)));
                col.setCellFactory(TextFieldTableCell.forTableColumn());
                col.setOnEditCommit(ev -> {
                    ObservableList<String> row = ev.getRowValue();
                    String oldVal = ev.getOldValue();
                    String newVal = ev.getNewValue();
                    if (!Objects.equals(oldVal, newVal)) {
                        Object pk = row.get(0);               // first column = PK
                        queries.updateRow(TABLE_NAME, Map.of(colName, newVal), pk);
                        row.set(idx, newVal);
                        dataStatusBar.setText("Updated " + colName);
                    }
                });

                SDSdataTable.getColumns().add(col);
            }

            SDSdataTable.setItems(fullData);
            SDSdataTable.setEditable(true);
            dataStatusBar.setText("Loaded \"" + TABLE_NAME + "\" (" + fullData.size() + " rows)");

        } catch (Exception e) {
            dataStatusBar.setText("Error loading table: " + e.getMessage());
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
                new FilteredList<>(fullData,
                        row -> row.stream().anyMatch(cell -> cell.toLowerCase().contains(q)));

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

    /* helpers */
    private void showInfo(String msg) {
        javafx.application.Platform.runLater(() ->
                new Alert(Alert.AlertType.INFORMATION, msg).showAndWait());
    }

    private void togglePanes() {
        boolean tableVisible = SDSdataTable.isVisible();
        SDSdataTable.setVisible(!tableVisible);
        SDSdataTable.setManaged(!tableVisible);

        sdsList.setVisible(tableVisible);
        sdsList.setManaged(tableVisible);
    }
}
