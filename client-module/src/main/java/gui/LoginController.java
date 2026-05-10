package gui;

import domain.User;
import domain.UserRole;
import exceptions.AppException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.IAuthService;
import service.IService;

public class LoginController extends BaseController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private IAuthService auth;
    private IService server;

    public void setServer(IService server) {
        this.server = server;
        this.auth = server;
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
            User loggedUser = auth.loginUser(user, pass, null);
            if (loggedUser != null) {
                openMainWindowFor(loggedUser);
            }
        } catch (AppException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleSignUp() {
        Stage owner = (Stage) usernameField.getScene().getWindow();
        User newUser = RegisterController.open(owner, server);
        if (newUser != null) {
            openMainWindowFor(newUser);
        }
    }

    public static void show(IService server) {
        LoginController controller = loadFxml("/loginView.fxml");
        controller.setServer(server);
        controller.showInNewStage("Login");
    }

    private void openMainWindowFor(User user) {
        usernameField.clear();
        passwordField.clear();
        if (user.getRole() == UserRole.CUSTOMER) {
            CustomerController.show(server, user);
        } else if (user.getRole() == UserRole.ADMIN) {
            AdminController.show(server, user);
        }
        closeWindowOf(usernameField);
    }
}
