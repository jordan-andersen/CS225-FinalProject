package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.QueryManager;
import model.MetadataService;


public class SDSController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private TableView <ObservableList<String>> SDSdataTable;
    @FXML private Label statusBar;

    private final QueryManager queries  = new QueryManager();
    private final MetadataService metadata = new MetadataService();
    private final ObservableList<ObservableList<String>> fullData = FXCollections.observableArrayList();


    @FXML
    public void initialize() {

    }

    private void loadTable(String tableName) {

    }

    private void doSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            SDSdataTable.setItems(fullData);
            statusBar.setText("Showing all rows");
            return;
        }

        FilteredList<ObservableList<String>> fl = new FilteredList<>(fullData,
                r -> r.stream().anyMatch(cell -> cell.toLowerCase().contains(q))
        );

        SDSdataTable.setItems(fl);
        statusBar.setText(fl.size() + " rows match \"" + q + "\"");
    }
}
