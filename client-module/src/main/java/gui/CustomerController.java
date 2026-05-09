package gui;

import domain.User;
import dtos.RouteDTO;
import dtos.TrainDTO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import service.IObserver;
import service.IService;

import java.io.IOException;

public class CustomerController implements IObserver {
    private IService server;
    private User currentUser;

    public void setServer(IService server) { this.server = server; }
    public void setUser(User user) { this.currentUser = user; }

    public static void show(IService server, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(CustomerController.class.getResource("/customerView.fxml"));
            Parent root = loader.load();

            CustomerController controller = loader.getController();
            controller.setServer(server);
            server.setObserver(controller);
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Train Ticketing — Customer · " + user.getUsername());
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

    @Override public void routeAdded(RouteDTO newRoute) {}
    @Override public void routeDeleted(RouteDTO oldRoute) {}
    @Override public void routeUpdated(RouteDTO updatedRoute) {}

    @Override public void trainAdded(TrainDTO newTrain) {}
    @Override public void trainDeleted(TrainDTO oldTrain) {}
    @Override public void trainUpdated(TrainDTO updatedTrain) {}
}
