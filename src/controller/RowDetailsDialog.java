package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Map;
import java.util.Objects;


// Simple read-only dialog showing every value of one row.

public class RowDetailsDialog extends Stage {
    public RowDetailsDialog(Window owner, String table, Map<String,Object> row) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Details – " + table);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        int r = 0;
        for (var entry : row.entrySet()) {
            Label key   = new Label(entry.getKey() + ":");
            key.setStyle("-fx-font-weight: bold");
            Label value = new Label(Objects.toString(entry.getValue(), ""));
            grid.addRow(r++, key, value);
        }

        Button close = new Button("Close");
        close.setOnAction(e -> close());

        BorderPane root = new BorderPane(grid, null, null, close, null);
        BorderPane.setMargin(close, new Insets(10));
        BorderPane.setAlignment(close, Pos.CENTER);

        setScene(new Scene(root));
    }
}
