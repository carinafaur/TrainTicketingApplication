package gui;

import domain.User;
import domain.UserRole;
import exceptions.AppException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
                usernameField.clear();
                passwordField.clear();
                if (loggedUser.getRole() == UserRole.CUSTOMER) CustomerController.show(server, loggedUser);
                else if (loggedUser.getRole() == UserRole.ADMIN) AdminController.show(server, loggedUser);
            }
        } catch (AppException e) {
            showError(e.getMessage());
        }
    }

    public static void show(IService server) {
        LoginController controller = loadFxml("/loginView.fxml");
        controller.setServer(server);
        controller.showInNewStage("Login");
    }
}
