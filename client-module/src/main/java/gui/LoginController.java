package gui;

import domain.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import domain.User;
import exceptions.AppException;
import javafx.stage.Stage;
import service.IObserver;
import service.IService;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private IService server;

    public void setServer(IService server) {
        this.server = server;
    }

    @FXML
    public void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Please fill all the fields!");
            return;
        }

        try {
            User loggedUser = server.loginUser(user, pass, null);

            if (loggedUser != null) {
                usernameField.clear();
                passwordField.clear();
                if(loggedUser.getRole()== UserRole.CUSTOMER)
                    openMainWindowCustomer(loggedUser);
                else if(loggedUser.getRole()== UserRole.ADMIN)
                    openMainWindowAdmin(loggedUser);
            }
        } catch (AppException e) {
            showError(e.getMessage());
        }
    }

    public static void show(IService server) {
        try {
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/loginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setServer(server);

            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError(e.getMessage());
        }
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openMainWindowCustomer(User loggedUser) {
        CustomerController.show(server, loggedUser);
    }

    private void openMainWindowAdmin(User loggedUser) {
       AdminController.show(server, loggedUser);
    }
}
