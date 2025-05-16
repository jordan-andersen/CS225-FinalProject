import controller.DashboardController;
import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;
import java.util.Optional;

/*
 * Main.MainApp is the entry point for the MassBay chemical inventory system.
 * It contains the main method where the application is launched, bringing the
 * user to first login to be able to access all features, and then to the Dashboard.
 *
 * by Bruna
 */

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        showLoginScreen();  // Show the login screen when the app starts
    }

    private void showLoginScreen() throws Exception {
        // Load the login.fxml from resources
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();  // Load the FXML file into the root element

        LoginController loginController = loader.getController();  // Get the controller instance for login view
        Stage loginStage = new Stage();
        loginStage.setScene(new Scene(root));  // Set the scene for the login window
        loginStage.setTitle("Login - MassBay Chemical Inventory System");
        loginStage.showAndWait();  // Show the login window and wait for the user to close it

        // After the login window is closed, check if the user has logged in successfully
        Optional<User> optUser = loginController.getAuthenticatedUser();  // Get the authenticated user from LoginController

        if (optUser.isPresent()) {
            // If login is successful, show the dashboard screen with the authenticated user
            showDashboardScreen(optUser.get());  // Pass the authenticated user to the next screen
        } else {
            // If login failed or was canceled, print a message and exit the application
            System.out.println("Login canceled or failed.");
            javafx.application.Platform.exit();  // Exit the application
        }
    }

    private void showDashboardScreen(User user) throws Exception {
        // Load the DashboardView.fxml from resources (ensure it's located under /resources/fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent root = loader.load();  // Load the FXML file into the root element

        // Get the controller instance for the Dashboard screen
        DashboardController controller = loader.getController();
        controller.setLoggedInUser(user);  // Pass the authenticated user to the DashboardController

        // Set the title and scene for the main stage (Dashboard Screen)
        primaryStage.setTitle("Dashboard – " + user.getName()); // Display the user's name on the title
        primaryStage.setScene(new Scene(root, 1000, 600));  // Set the dashboard screen's scene with a specific size
        primaryStage.show();  // Show the main dashboard screen
    }

    public void logout() throws Exception {
        primaryStage.hide();
        showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);  // Launch the application (calls start() method)
    }
}
