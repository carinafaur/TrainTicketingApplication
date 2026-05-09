package gui;

import domain.Route;
import domain.Station;
import domain.Train;
import domain.User;
import dtos.BookingDTO;
import dtos.RouteDTO;
import dtos.ScheduleDTO;
import dtos.StationDTO;
import dtos.TrainDTO;
import exceptions.AppException;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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
import networking.ServerProxy;
import service.IObserver;
import service.IService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class AdminController implements IObserver {

    private static final DateTimeFormatter UI_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private IService server;
    private User currentUser;

    private final ObservableList<Route> routesModel = FXCollections.observableArrayList();
    private final ObservableList<Station> stationsModel = FXCollections.observableArrayList();
    private final ObservableList<Train> trainsModel = FXCollections.observableArrayList();
    private final ObservableList<ScheduleDTO> schedulesModel = FXCollections.observableArrayList();
    private final ObservableList<BookingDTO> allBookingsModel = FXCollections.observableArrayList();
    private final ObservableList<BookingDTO> bookingsModel = FXCollections.observableArrayList();

    @FXML private TableView<Route> routesTable;
    @FXML private TableColumn<Route, Integer> routeIdColumn;
    @FXML private TableColumn<Route, String>  routeStartColumn;
    @FXML private TableColumn<Route, String>  routeEndColumn;
    @FXML private ComboBox<Station> startStationCombo;
    @FXML private ComboBox<Station> endStationCombo;
    @FXML private Label messageLabel;

    @FXML private TableView<Train> trainsTable;
    @FXML private TableColumn<Train, Integer> trainIdColumn;
    @FXML private TableColumn<Train, String>  trainNumberColumn;
    @FXML private TableColumn<Train, Integer> trainCapacityColumn;
    @FXML private TextField trainNumberField;
    @FXML private Spinner<Integer> trainCapacitySpinner;
    @FXML private Label trainMessageLabel;

    @FXML private TableView<Station> stationsTable;
    @FXML private TableColumn<Station, Integer> stationIdColumn;
    @FXML private TableColumn<Station, String>  stationCityColumn;
    @FXML private TableColumn<Station, String>  stationNameColumn;
    @FXML private TextField stationCityField;
    @FXML private TextField stationNameField;
    @FXML private Label stationMessageLabel;

    @FXML private ComboBox<Train> scheduleTrainCombo;
    @FXML private ComboBox<Route> scheduleRouteCombo;
    @FXML private TableView<ScheduleDTO> schedulesTable;
    @FXML private TableColumn<ScheduleDTO, Integer> schedIdColumn;
    @FXML private TableColumn<ScheduleDTO, String>  schedTrainColumn;
    @FXML private TableColumn<ScheduleDTO, String>  schedRouteColumn;
    @FXML private TableColumn<ScheduleDTO, String>  schedDepartureColumn;
    @FXML private TableColumn<ScheduleDTO, String>  schedArrivalColumn;
    @FXML private TableColumn<ScheduleDTO, Integer> schedStopsColumn;
    @FXML private TableColumn<ScheduleDTO, Integer> schedDelayColumn;
    @FXML private TableColumn<ScheduleDTO, String>  schedStatusColumn;
    @FXML private Label scheduleMessageLabel;

    @FXML private ComboBox<Train> bookingsTrainFilter;
    @FXML private TableView<BookingDTO> bookingsTable;
    @FXML private TableColumn<BookingDTO, Integer> bookIdColumn;
    @FXML private TableColumn<BookingDTO, String>  bookUserColumn;
    @FXML private TableColumn<BookingDTO, String>  bookTrainColumn;
    @FXML private TableColumn<BookingDTO, String>  bookFromColumn;
    @FXML private TableColumn<BookingDTO, String>  bookToColumn;
    @FXML private TableColumn<BookingDTO, String>  bookDepartureColumn;
    @FXML private TableColumn<BookingDTO, Integer> bookSeatsColumn;
    @FXML private TableColumn<BookingDTO, String>  bookBookedAtColumn;
    @FXML private Label bookingsHint;

    @FXML private Label welcomeLabel;

    public void setServer(IService server) {
        this.server = server;
        this.server.setObserver(this);
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Signed in as " + user.getUsername() + " · manage routes, trains and schedules");
        }
        initData();
    }

    @FXML
    public void initialize() {
        routeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        routeStartColumn.setCellValueFactory(new PropertyValueFactory<>("startCity"));
        routeEndColumn.setCellValueFactory(new PropertyValueFactory<>("endCity"));
        routesTable.setItems(routesModel);

        StringConverter<Station> stationCityConverter = new StringConverter<>() {
            @Override public String toString(Station s) { return s == null ? "" : s.getStationCity(); }
            @Override public Station fromString(String s) { return null; }
        };
        startStationCombo.setConverter(stationCityConverter);
        endStationCombo.setConverter(stationCityConverter);
        startStationCombo.setItems(stationsModel);
        endStationCombo.setItems(stationsModel);

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

        stationIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        stationCityColumn.setCellValueFactory(new PropertyValueFactory<>("stationCity"));
        stationNameColumn.setCellValueFactory(new PropertyValueFactory<>("stationName"));
        stationsTable.setItems(stationsModel);

        stationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                handleClearStationForm();
            } else {
                stationCityField.setText(newV.getStationCity());
                stationNameField.setText(newV.getStationName());
            }
        });

        routesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                startStationCombo.getSelectionModel().clearSelection();
                endStationCombo.getSelectionModel().clearSelection();
                return;
            }
            selectStationInCombo(startStationCombo, newV.getStartStation());
            selectStationInCombo(endStationCombo, newV.getEndStation());
        });

        scheduleTrainCombo.setItems(trainsModel);
        scheduleTrainCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Train t) { return t == null ? "" : t.getTrainNumber() + " (cap " + t.getCapacity() + ")"; }
            @Override public Train fromString(String s) { return null; }
        });

        scheduleRouteCombo.setItems(routesModel);
        scheduleRouteCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Route r) {
                if (r == null) return "";
                return r.getStartStation().getStationCity() + " → " + r.getEndStation().getStationCity();
            }
            @Override public Route fromString(String s) { return null; }
        });

        schedIdColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        schedTrainColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrainNumber()));
        schedRouteColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRouteLabel()));
        schedDepartureColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDepartureTime() == null ? "" : c.getValue().getDepartureTime().format(UI_FMT)));
        schedArrivalColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getArrivalTime() == null ? "" : c.getValue().getArrivalTime().format(UI_FMT)));
        schedStopsColumn.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getStops() == null ? 0 : c.getValue().getStops().size()).asObject());
        schedDelayColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDelayMinutes()).asObject());
        schedStatusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        schedulesTable.setItems(schedulesModel);

        bookingsTrainFilter.setItems(trainsModel);
        bookingsTrainFilter.setConverter(new StringConverter<>() {
            @Override public String toString(Train t) { return t == null ? "All trains" : t.getTrainNumber(); }
            @Override public Train fromString(String s) { return null; }
        });
        bookingsTrainFilter.valueProperty().addListener((obs, oldV, newV) -> applyBookingsFilter());

        bookIdColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        bookUserColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        bookTrainColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrainNumber()));
        bookFromColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStartStationCity() + " · " + c.getValue().getStartStationName()));
        bookToColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEndStationCity() + " · " + c.getValue().getEndStationName()));
        bookDepartureColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getScheduleDeparture() == null ? "" : c.getValue().getScheduleDeparture().format(UI_FMT)));
        bookSeatsColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSeatsReserved()).asObject());
        bookBookedAtColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getBookingDate() == null ? "" : c.getValue().getBookingDate().format(UI_FMT)));
        bookingsTable.setItems(bookingsModel);
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
        loadSchedules();
        loadBookings();
    }

    private void loadRoutes()    { routesModel.setAll(server.getAllRoutes()); }
    private void loadStations()  { stationsModel.setAll(server.getAllStations()); }
    private void loadTrains()    { trainsModel.setAll(server.getAllTrains()); }
    private void loadSchedules() {
        if (server instanceof ServerProxy) {
            schedulesModel.setAll(((ServerProxy) server).getAllScheduleDTOs());
        }
    }
    private void loadBookings() {
        allBookingsModel.setAll(server.getAllBookings());
        applyBookingsFilter();
    }

    private void applyBookingsFilter() {
        Train filter = bookingsTrainFilter == null ? null : bookingsTrainFilter.getValue();
        if (filter == null) {
            bookingsModel.setAll(allBookingsModel);
        } else {
            bookingsModel.setAll(allBookingsModel.filtered(b -> filter.getTrainNumber().equals(b.getTrainNumber())));
        }
        if (bookingsHint != null) {
            bookingsHint.setText(bookingsModel.size() + " booking(s)"
                    + (filter == null ? "" : " for train " + filter.getTrainNumber()));
        }
    }

    @FXML
    public void handleClearBookingsFilter() {
        if (bookingsTrainFilter != null) bookingsTrainFilter.setValue(null);
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
            loadRoutes();
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
            loadRoutes();
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
            loadRoutes();
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

    @FXML
    public void handleAddTrain() {
        try {
            Train t = new Train(safeNumber(), safeCapacity());
            server.addTrain(t);
            loadTrains();
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
            loadTrains();
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
            loadTrains();
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

    @FXML
    public void handleAddStation() {
        try {
            Station s = new Station(safeStationName(), safeStationCity());
            server.addStation(s);
            loadStations();
            setOk(stationMessageLabel, "Station '" + s.getStationCity() + " · " + s.getStationName() + "' added.");
            handleClearStationForm();
        } catch (Exception e) {
            setErr(stationMessageLabel, "Failed to add station: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateStation() {
        Station selected = stationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select a station to modify!"); return; }
        try {
            Station edited = new Station(safeStationName(), safeStationCity());
            edited.setId(selected.getId());
            server.updateStation(edited);
            loadStations();
            setOk(stationMessageLabel, "Station updated.");
        } catch (Exception e) {
            setErr(stationMessageLabel, "Update failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteStation() {
        Station selected = stationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select a station to delete!"); return; }
        try {
            server.removeStation(selected);
            loadStations();
            setOk(stationMessageLabel, "Station deleted.");
            handleClearStationForm();
        } catch (Exception e) {
            setErr(stationMessageLabel, "Delete failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleClearStationForm() {
        stationCityField.clear();
        stationNameField.clear();
        stationsTable.getSelectionModel().clearSelection();
    }

    private String safeStationName() {
        return stationNameField.getText() == null ? "" : stationNameField.getText().trim();
    }
    private String safeStationCity() {
        return stationCityField.getText() == null ? "" : stationCityField.getText().trim();
    }

    private String safeNumber() { return trainNumberField.getText() == null ? "" : trainNumberField.getText().trim(); }
    private int safeCapacity() {
        Integer v = trainCapacitySpinner.getValue();
        return v == null ? 0 : v;
    }

    @FXML
    public void handleStartNewSchedule() {
        Train t = scheduleTrainCombo.getValue();
        Route r = scheduleRouteCombo.getValue();
        if (t == null || r == null) {
            showError("Pick a train and a route first.");
            return;
        }
        try {
            Stage owner = (Stage) schedulesTable.getScene().getWindow();
            ScheduleDTO created = ScheduleEditController.openForNew(owner, stationsModel, t, r);
            if (created != null) {
                ((ServerProxy) server).addScheduleDTO(created);
                loadSchedules();
                setOk(scheduleMessageLabel, "Schedule created.");
            }
        } catch (AppException e) {
            setErr(scheduleMessageLabel, "Failed to add schedule: " + e.getMessage());
        } catch (IOException e) {
            showError("Couldn't open dialog: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditSchedule() {
        ScheduleDTO selected = schedulesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Pick a schedule to edit."); return; }
        try {
            Stage owner = (Stage) schedulesTable.getScene().getWindow();
            ScheduleDTO updated = ScheduleEditController.openForEdit(owner, stationsModel, selected);
            if (updated != null) {
                ((ServerProxy) server).updateScheduleDTO(updated);
                loadSchedules();
                setOk(scheduleMessageLabel, "Schedule updated.");
            }
        } catch (AppException e) {
            setErr(scheduleMessageLabel, "Update failed: " + e.getMessage());
        } catch (IOException e) {
            showError("Couldn't open dialog: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteSchedule() {
        ScheduleDTO selected = schedulesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Pick a schedule to delete."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete schedule");
        confirm.setHeaderText("Delete schedule #" + selected.getId() + "?");
        confirm.setContentText(selected.getTrainNumber() + " · " + selected.getRouteLabel());
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    ((ServerProxy) server).removeScheduleDTO(selected);
                    loadSchedules();
                    setOk(scheduleMessageLabel, "Schedule deleted.");
                } catch (AppException e) {
                    setErr(scheduleMessageLabel, "Delete failed: " + e.getMessage());
                }
            }
        });
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

    @Override public void routeAdded(RouteDTO newRoute)         { Platform.runLater(this::loadRoutes); }
    @Override public void routeDeleted(RouteDTO oldRoute)       { Platform.runLater(() -> routesModel.removeIf(r -> r.getId() == oldRoute.getId())); }
    @Override public void routeUpdated(RouteDTO updatedRoute)   { Platform.runLater(this::loadRoutes); }

    @Override public void stationAdded(StationDTO newStation)   { Platform.runLater(this::loadStations); }
    @Override public void stationDeleted(StationDTO oldStation) { Platform.runLater(() -> stationsModel.removeIf(s -> s.getId() == oldStation.getId())); }
    @Override public void stationUpdated(StationDTO updS)       { Platform.runLater(this::loadStations); }

    @Override public void trainAdded(TrainDTO newTrain)         { Platform.runLater(this::loadTrains); }
    @Override public void trainDeleted(TrainDTO oldTrain)       { Platform.runLater(() -> trainsModel.removeIf(t -> t.getId() == oldTrain.getId())); }
    @Override public void trainUpdated(TrainDTO updatedTrain)   { Platform.runLater(this::loadTrains); }

    @Override public void scheduleAdded(ScheduleDTO newS)       { Platform.runLater(this::loadSchedules); }
    @Override public void scheduleDeleted(ScheduleDTO oldS)     { Platform.runLater(() -> schedulesModel.removeIf(s -> s.getId() == oldS.getId())); }
    @Override public void scheduleUpdated(ScheduleDTO updS)     { Platform.runLater(this::loadSchedules); }

    @Override public void bookingAdded(BookingDTO newBooking)   { Platform.runLater(this::loadBookings); }

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
