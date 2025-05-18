package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.ColumnData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRowDialog extends Stage {

    private final Map<String, TextField> inputs = new HashMap<>();

    public AddRowDialog(Window owner,
                        String table,
                        List<ColumnData> columns,
                        OnSave handler) {

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Add new entry – " + table);

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(15);
        grid.setPadding(new Insets(15));

        int r = 0;
        for (ColumnData col : columns) {
            // skip AUTOINCREMENT primary key
            if (col.isPrimaryKey() && col.getType().equalsIgnoreCase("COUNTER"))
                continue;

            Label lbl = new Label(col.getName() + ":");
            TextField tf = new TextField();
            tf.setPrefWidth(240);

            inputs.put(col.getName(), tf);
            grid.addRow(r++, lbl, tf);
        }

        Button save = new Button("Save");
        save.setDefaultButton(true);
        save.setOnAction(e -> {
            Map<String, Object> values = new HashMap<>();
            inputs.forEach((k, tf) -> {
                String v = tf.getText().trim();
                values.put(k, v.isEmpty() ? null : v);
            });
            handler.save(values);
            close();
        });

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> close());

        ToolBar bar = new ToolBar(save, cancel);
        bar.setPadding(new Insets(10));
        bar.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        bar.setPrefHeight(40);
        BorderPane.setAlignment(bar, Pos.CENTER_RIGHT);

        javafx.scene.layout.BorderPane root =
                new javafx.scene.layout.BorderPane(grid, null, null, bar, null);

        setScene(new Scene(root));
    }

    /** callback functional-interface so InventoryController can act on save */
    @FunctionalInterface public interface OnSave { void save(Map<String, Object> values); }
}