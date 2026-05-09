package gui;

import domain.Route;
import domain.Station;
import domain.Train;
import domain.User;
import dtos.RouteDTO;
import dtos.TrainDTO;
import javafx.application.Platform;
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

    // ---------- Routes tab ----------
    private final ObservableList<Route> routesModel = FXCollections.observableArrayList();
    private final ObservableList<Station> stationsModel = FXCollections.observableArrayList();

    @FXML private TableView<Route> routesTable;
    @FXML private TableColumn<Route, Integer> routeIdColumn;
    @FXML private TableColumn<Route, String>  routeStartColumn;
    @FXML private TableColumn<Route, String>  routeEndColumn;

    @FXML private ComboBox<Station> startStationCombo;
    @FXML private ComboBox<Station> endStationCombo;
    @FXML private Label messageLabel;

    // ---------- Trains tab ----------
    private final ObservableList<Train> trainsModel = FXCollections.observableArrayList();

    @FXML private TableView<Train> trainsTable;
    @FXML private TableColumn<Train, Integer> trainIdColumn;
    @FXML private TableColumn<Train, String>  trainNumberColumn;
    @FXML private TableColumn<Train, Integer> trainCapacityColumn;

    @FXML private TextField trainNumberField;
    @FXML private Spinner<Integer> trainCapacitySpinner;
    @FXML private Label trainMessageLabel;

    @FXML private Label welcomeLabel;

    // ====================================================================== setup

    public void setServer(IService server) {
        this.server = server;
        this.server.setObserver(this);
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Signed in as " + user.getUsername() + " · manage routes and trains");
        }
        initData();
    }

    @FXML
    public void initialize() {
        // ---- Routes table
        routeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        routeStartColumn.setCellValueFactory(new PropertyValueFactory<>("startCity"));
        routeEndColumn.setCellValueFactory(new PropertyValueFactory<>("endCity"));
        routesTable.setItems(routesModel);

        StringConverter<Station> stationConverter = new StringConverter<>() {
            @Override public String toString(Station s) { return s == null ? "" : s.getStationCity(); }
            @Override public Station fromString(String s) { return null; }
        };
        startStationCombo.setConverter(stationConverter);
        endStationCombo.setConverter(stationConverter);
        startStationCombo.setItems(stationsModel);
        endStationCombo.setItems(stationsModel);

        // ---- Trains table
        trainIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        trainNumberColumn.setCellValueFactory(new PropertyValueFactory<>("trainNumber"));
        trainCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        trainsTable.setItems(trainsModel);

        trainCapacitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 2000, 100, 10));

        trainsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                handleClearTrainForm();
            } else {
                trainNumberField.setText(newV.getTrainNumber());
                trainCapacitySpinner.getValueFactory().setValue(newV.getCapacity());
            }
        });

        // Auto-populate combos when a route is selected in the table.
        routesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                startStationCombo.getSelectionModel().clearSelection();
                endStationCombo.getSelectionModel().clearSelection();
                return;
            }
            selectStationInCombo(startStationCombo, newV.getStartStation());
            selectStationInCombo(endStationCombo, newV.getEndStation());
        });
    }

    private void selectStationInCombo(ComboBox<Station> combo, Station target) {
        if (target == null) { combo.getSelectionModel().clearSelection(); return; }
        for (Station s : combo.getItems()) {
            if (s != null && s.getId() == target.getId()) {
                combo.getSelectionModel().select(s);
                return;
            }
        }
        combo.getSelectionModel().clearSelection();
    }

    public static void show(IService server, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(AdminController.class.getResource("/adminView.fxml"));
            Parent root = loader.load();

            AdminController controller = loader.getController();
            controller.setServer(server);
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Train Ticketing — Admin · " + user.getUsername());
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
        loadTrains();
    }

    private void loadRoutes()   { routesModel.setAll(server.getAllRoutes()); }
    private void loadStations() { stationsModel.setAll(server.getAllStations()); }
    private void loadTrains()   { trainsModel.setAll(server.getAllTrains()); }

    // ====================================================================== Routes

    @FXML
    public void handleAddRoute() {
        Station start = startStationCombo.getSelectionModel().getSelectedItem();
        Station end   = endStationCombo.getSelectionModel().getSelectedItem();
        if (start == null || end == null || start.getId() == end.getId()) {
            showError("Please select valid start and end stations!");
            return;
        }
        try {
            server.addRoute(new Route(start, end));
            setOk(messageLabel, "Route added.");
        } catch (Exception e) {
            setErr(messageLabel, "Failed to add route: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateRoute() {
        Route selected = routesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select a route to update!"); return; }
        try {
            Station start = startStationCombo.getSelectionModel().getSelectedItem();
            Station end   = endStationCombo.getSelectionModel().getSelectedItem();
            selected.setStartStation(start);
            selected.setEndStation(end);
            server.updateRoute(selected);
            setOk(messageLabel, "Route updated.");
        } catch (Exception e) {
            setErr(messageLabel, "Update failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteRoute() {
        Route selected = routesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select a route to delete!"); return; }
        try {
            server.removeRoute(selected);
            setOk(messageLabel, "Route deleted.");
        } catch (Exception e) {
            setErr(messageLabel, "Delete failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleClearRouteForm() {
        startStationCombo.getSelectionModel().clearSelection();
        endStationCombo.getSelectionModel().clearSelection();
        routesTable.getSelectionModel().clearSelection();
    }

    // ====================================================================== Trains

    @FXML
    public void handleAddTrain() {
        try {
            Train t = new Train(safeNumber(), safeCapacity());
            server.addTrain(t);
            setOk(trainMessageLabel, "Train '" + t.getTrainNumber() + "' added.");
            handleClearTrainForm();
        } catch (Exception e) {
            setErr(trainMessageLabel, "Failed to add train: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateTrain() {
        Train selected = trainsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select a train to modify!"); return; }
        try {
            Train edited = new Train(safeNumber(), safeCapacity());
            edited.setId(selected.getId());
            server.updateTrain(edited);
            setOk(trainMessageLabel, "Train '" + edited.getTrainNumber() + "' updated.");
        } catch (Exception e) {
            setErr(trainMessageLabel, "Update failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteTrain() {
        Train selected = trainsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select a train to delete!"); return; }
        try {
            server.removeTrain(selected);
            setOk(trainMessageLabel, "Train '" + selected.getTrainNumber() + "' deleted.");
            handleClearTrainForm();
        } catch (Exception e) {
            setErr(trainMessageLabel, "Delete failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleClearTrainForm() {
        trainNumberField.clear();
        trainCapacitySpinner.getValueFactory().setValue(100);
        trainsTable.getSelectionModel().clearSelection();
    }

    private String safeNumber() { return trainNumberField.getText() == null ? "" : trainNumberField.getText().trim(); }
    private int safeCapacity() {
        Integer v = trainCapacitySpinner.getValue();
        return v == null ? 0 : v;
    }

    // ====================================================================== Logout

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

    // ====================================================================== Observer

    @Override
    public void routeAdded(RouteDTO newRoute) {
        Platform.runLater(this::loadRoutes);
    }
    @Override
    public void routeDeleted(RouteDTO oldRoute) {
        Platform.runLater(() -> routesModel.removeIf(r -> r.getId() == oldRoute.getId()));
    }
    @Override
    public void routeUpdated(RouteDTO updatedRoute) {
        Platform.runLater(this::loadRoutes);
    }

    @Override
    public void trainAdded(TrainDTO newTrain) {
        Platform.runLater(this::loadTrains);
    }
    @Override
    public void trainDeleted(TrainDTO oldTrain) {
        Platform.runLater(() -> trainsModel.removeIf(t -> t.getId() == oldTrain.getId()));
    }
    @Override
    public void trainUpdated(TrainDTO updatedTrain) {
        Platform.runLater(this::loadTrains);
    }

    // ====================================================================== Helpers

    private void setOk(Label l, String msg) {
        if (l == null) return;
        l.getStyleClass().removeAll("status-ok", "status-error");
        l.getStyleClass().add("status-ok");
        l.setText(msg);
    }
    private void setErr(Label l, String msg) {
        if (l == null) return;
        l.getStyleClass().removeAll("status-ok", "status-error");
        l.getStyleClass().add("status-error");
        l.setText(msg);
    }
}
