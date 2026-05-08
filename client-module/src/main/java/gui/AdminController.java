package gui;

import domain.Route;
import domain.Station;
import domain.User;
import dtos.RouteDTO;
import exceptions.AppException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import service.IObserver;
import service.IService;

import java.io.IOException;
import java.util.List;

public class AdminController implements IObserver {
    private IService server;
    private User currentUser;

    private final ObservableList<Route> routesModel = FXCollections.observableArrayList();
    private final ObservableList<Station> stationsModel = FXCollections.observableArrayList();

    @FXML
    private TextField startStationField;
    @FXML
    private TextField endStationField;

    @FXML
    private TableView<Route> routesTable;
    @FXML
    private TableColumn<Route, Integer> routeIdColumn;
    @FXML
    private TableColumn<Route, String> routeStartColumn;
    @FXML
    private TableColumn<Route, String> routeEndColumn;

    @FXML
    private ComboBox<Station> startStationCombo;
    @FXML
    private ComboBox<Station> endStationCombo;

    @FXML
    Label messageLabel;

    public void setServer(IService server) {
        this.server = server;
        this.server.setObserver(this);
    }

    public void setUser(User user) {
        this.currentUser = user;
        initData();
    }

    @FXML
    public void initialize() {
        routeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        routeStartColumn.setCellValueFactory(new PropertyValueFactory<>("startCity"));
        routeEndColumn.setCellValueFactory(new PropertyValueFactory<>("endCity"));

        routesTable.setItems(routesModel);

        StringConverter<Station> stationConverter = new StringConverter<>() {
            @Override
            public String toString(Station station) {

                return (station == null) ? "" : station.getStationCity();
            }

            @Override
            public Station fromString(String string) {
                return null;
            }
        };

        startStationCombo.setConverter(stationConverter);
        endStationCombo.setConverter(stationConverter);
        startStationCombo.setItems(stationsModel);
        endStationCombo.setItems(stationsModel);
    }

    public static void show(IService server, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(AdminController.class.getResource("/adminView.fxml"));
            Parent root = loader.load();

            AdminController controller = loader.getController();
            controller.setServer(server);
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Booking System - " + user.getUsername());
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

    private void initData() {
        loadRoutes();
        loadStations();
    }

    private void loadRoutes() {
        List<Route> all = server.getAllRoutes();
        routesModel.setAll(all);
    }

    private void loadStations() {
        List<Station> all = server.getAllStations();
        stationsModel.setAll(all);
    }

    @FXML
    public void handleAddRoute() {
        Station startStation = (Station) startStationCombo.getSelectionModel().getSelectedItem();
        Station endStation = (Station) endStationCombo.getSelectionModel().getSelectedItem();

        if (startStation == null || endStation == null || startStation.getId() == endStation.getId()) {
            showError("Please select valid start and end stations!");
            return;
        }

        try {
            Route route = new Route(startStation, endStation);
            server.addRoute(route);
            messageLabel.setText("Route added successfully!");
        } catch (AppException e) {
            showError("Failed to add route: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateRoute() {
        Route selected = routesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a route to update!");
            return;
        }
        try {
            Station startStation = (Station) startStationCombo.getSelectionModel().getSelectedItem();
            Station endStation = (Station) endStationCombo.getSelectionModel().getSelectedItem();
            selected.setStartStation(startStation);
            selected.setEndStation(endStation);
            server.updateRoute(selected);
            messageLabel.setText("Route updated!");
        } catch (Exception e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteRoute() {
        Route selected = routesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                server.removeRoute(selected);
                messageLabel.setText("Route deleted.");
            } catch (Exception e) {
                showError("Delete failed: " + e.getMessage());
            }
        }
    }


    @FXML
    public void handleLogout() {
        try {
            server.logoutUser(currentUser.getUsername(), this);
            Stage stage = (Stage) routesTable.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            showError("Logout error: " + e.getMessage());
        }
    }

    @Override
    public void routeAdded(RouteDTO newRoute) {
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(100);
                loadRoutes();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void routeDeleted(RouteDTO oldRoute) {
        javafx.application.Platform.runLater(()->{
            javafx.application.Platform.runLater(() -> {
                routesModel.removeIf(r -> r.getId() == oldRoute.getId());
            });
        });
    }

    @Override
    public void routeUpdated(RouteDTO updatedRoute) {
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(100);
                loadRoutes();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
