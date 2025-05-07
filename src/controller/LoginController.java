package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.User;
import model.UserManager;
import javafx.stage.Stage;
import java.util.Optional;

/*
 * LoginController is for the login screen of the chemical inventory system.
 * it handles credentials input validation, and screen transitions.
 *
 * first implemented by Bruna
 */

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private final UserManager userManager = new UserManager();
    private User authenticatedUser = null;

    public Optional<User> getAuthenticatedUser() {
        return Optional.ofNullable(authenticatedUser);
    }

    @FXML
    private void handleLogin() {
        String u = usernameField.getText();
        String p = passwordField.getText();

        User user = userManager.verifyLogin(u, p);
        if (user != null) {
            authenticatedUser = user;
            //closes login window and return control to MainApp
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText(" Incorrect username or password.");
            alert.showAndWait();
            usernameField.clear();
            passwordField.clear();
        }
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText(null);
        alert.setContentText("Contact your administrator.");
        alert.showAndWait();
    }
}
