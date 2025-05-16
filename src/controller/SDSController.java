package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import model.PubChemService;
public class SDSController {

    @FXML
    public Button searchButton;
    @FXML
    public TextField searchField;

    @FXML
    public void initialize() {
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSearch();
            }
        });
    }

    @FXML
    public void handleSearch() {
        new PubChemService().openSDSPage(searchField.getText().trim());
    }
}
