package gui;

import domain.User;
import domain.UserRole;
import dtos.RegisterRequestDTO;
import exceptions.AppException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.IAuthService;
import service.IService;

public class RegisterController extends BaseController {

    @FXML private ComboBox<UserRole> roleCombo;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private IAuthService auth;
    private User registeredUser;

    public User getRegisteredUser() { return registeredUser; }

    public void setServer(IService server) {
        this.auth = server;
    }

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(UserRole.CUSTOMER, UserRole.ADMIN));
        roleCombo.getSelectionModel().select(UserRole.CUSTOMER);
        errorLabel.setText("");
    }

    @FXML
    public void handleRegister() {
        UserRole role = roleCombo.getValue();
        String username = textOf(usernameField);
        String email = textOf(emailField);
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (role == null) {
            setErr(errorLabel, "Pick a role.");
            return;
        }
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            setErr(errorLabel, "Fill all fields.");
            return;
        }
        if (!password.equals(confirm)) {
            setErr(errorLabel, "Passwords do not match.");
            return;
        }

        try {
            this.registeredUser = auth.registerUser(
                    new RegisterRequestDTO(username, password, email, role),
                    null
            );
            stage.close();
        } catch (AppException e) {
            setErr(errorLabel, e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        this.registeredUser = null;
        stage.close();
    }

    public static User open(Stage owner, IService server) {
        RegisterController controller = loadFxml("/registerView.fxml");
        controller.setServer(server);
        controller.showAsModalDialog("Create account", owner);
        return controller.getRegisteredUser();
    }

    private static String textOf(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}
