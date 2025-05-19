package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author Lucas L., Abraham A., Jordan A.
 */
public class SDSController {

    @FXML private TextField     searchField;
    @FXML private Button        searchButton;
    @FXML private ListView<Path> sdsList;
    @FXML private Label         statusBar;

    private static final Path SDS_DIR = Paths.get("data", "SDS");
    private final ObservableList<Path> fullList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadDirectory();

        searchField.setOnAction(e -> doSearch());
        searchButton.setOnAction(e -> doSearch());

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
            statusBar.setText("Directory not found: " + SDS_DIR.toAbsolutePath());
            return;
        }
        try (var stream = Files.list(SDS_DIR)) {
            fullList.addAll(stream
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .collect(Collectors.toList()));
        } catch (IOException ex) {
            statusBar.setText("Error reading directory: " + ex.getMessage());
            return;
        }

        sdsList.setItems(fullList);
        statusBar.setText(fullList.size() + " files");
    }

    private void doSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            sdsList.setItems(fullList);
            statusBar.setText("Showing all files");
            return;
        }
        FilteredList<Path> filtered =
                new FilteredList<>(fullList,
                        p -> p.getFileName().toString().toLowerCase().contains(q));

        sdsList.setItems(filtered);
        statusBar.setText(filtered.size() + " files match \"" + q + "\"");
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
}
