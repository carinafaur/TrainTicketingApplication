package gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Common scaffolding for every JavaFX controller in the app:
 * FXML loading, stage creation (top-level + modal), error alerts,
 * status-label helpers and a shared date formatter.
 *
 * <p>Concrete controllers extend this class. Static {@code show(...)}
 * factory methods on each subclass typically call
 * {@link #loadFxml(String)} to instantiate, configure dependencies, and
 * then either {@link #showInNewStage(String)} or
 * {@link #showAsModalDialog(String, Stage)} to display.
 */
public abstract class BaseController {

    protected static final DateTimeFormatter UI_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    protected Parent root;
    protected Stage stage;

    @SuppressWarnings("unchecked")
    protected static <T extends BaseController> T loadFxml(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(BaseController.class.getResource(fxmlPath));
            Parent root = loader.load();
            T controller = (T) loader.getController();
            controller.root = root;
            return controller;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    protected Stage showInNewStage(String title) {
        this.stage = new Stage();
        this.stage.setTitle(title);
        this.stage.setScene(new Scene(root));
        this.stage.show();
        return this.stage;
    }

    protected Stage showAsModalDialog(String title, Stage owner) {
        this.stage = new Stage();
        this.stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) this.stage.initOwner(owner);
        this.stage.setTitle(title);
        this.stage.setScene(new Scene(root));
        this.stage.showAndWait();
        return this.stage;
    }

    protected void closeWindowOf(Node anchor) {
        if (anchor != null && anchor.getScene() != null) {
            ((Stage) anchor.getScene().getWindow()).close();
        } else if (this.stage != null) {
            this.stage.close();
        }
    }

    protected static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void setOk(Label l, String msg) {
        if (l == null) return;
        l.getStyleClass().removeAll("status-ok", "status-error");
        l.getStyleClass().add("status-ok");
        l.setText(msg);
    }

    protected void setErr(Label l, String msg) {
        if (l == null) return;
        l.getStyleClass().removeAll("status-ok", "status-error");
        l.getStyleClass().add("status-error");
        l.setText(msg);
    }
}
