package gui;

import domain.Station;
import domain.User;
import dtos.AvailableScheduleDTO;
import dtos.BookingDTO;
import dtos.BookingRequestDTO;
import dtos.JourneySearchDTO;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import service.IObserver;
import service.IService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomerController implements IObserver {

    private static final DateTimeFormatter UI_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private IService server;
    private User currentUser;

    private final ObservableList<Station> stationsModel = FXCollections.observableArrayList();
    private final ObservableList<AvailableScheduleDTO> resultsModel = FXCollections.observableArrayList();
    private final ObservableList<BookingDTO> myBookingsModel = FXCollections.observableArrayList();

    @FXML private Label welcomeLabel;

    @FXML private ComboBox<Station> searchFromCombo;
    @FXML private ComboBox<Station> searchToCombo;
    @FXML private DatePicker searchDate;

    @FXML private Label resultsHint;
    @FXML private TableView<AvailableScheduleDTO> resultsTable;
    @FXML private TableColumn<AvailableScheduleDTO, String>  resTrainColumn;
    @FXML private TableColumn<AvailableScheduleDTO, String>  resFromColumn;
    @FXML private TableColumn<AvailableScheduleDTO, String>  resDepartureColumn;
    @FXML private TableColumn<AvailableScheduleDTO, String>  resToColumn;
    @FXML private TableColumn<AvailableScheduleDTO, String>  resArrivalColumn;
    @FXML private TableColumn<AvailableScheduleDTO, Integer> resSeatsColumn;
    @FXML private TableColumn<AvailableScheduleDTO, String>  resStatusColumn;

    @FXML private Spinner<Integer> seatsSpinner;
    @FXML private TextField emailField;
    @FXML private Label bookingMessageLabel;

    @FXML private TableView<BookingDTO> myBookingsTable;
    @FXML private TableColumn<BookingDTO, Integer> myIdColumn;
    @FXML private TableColumn<BookingDTO, String>  myTrainColumn;
    @FXML private TableColumn<BookingDTO, String>  myFromColumn;
    @FXML private TableColumn<BookingDTO, String>  myToColumn;
    @FXML private TableColumn<BookingDTO, String>  myDepartureColumn;
    @FXML private TableColumn<BookingDTO, Integer> mySeatsColumn;
    @FXML private TableColumn<BookingDTO, String>  myBookedAtColumn;

    public void setServer(IService server) {
        this.server = server;
        this.server.setObserver(this);
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Signed in as " + user.getUsername()
                    + " · search journeys and book tickets");
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailField.setText(user.getEmail());
        }
        loadStations();
        loadMyBookings();
    }

    @FXML
    public void initialize() {
        StringConverter<Station> stationConverter = new StringConverter<>() {
            @Override public String toString(Station s) { return s == null ? "" : s.getStationCity() + " · " + s.getStationName(); }
            @Override public Station fromString(String s) { return null; }
        };
        searchFromCombo.setConverter(stationConverter);
        searchToCombo.setConverter(stationConverter);
        searchFromCombo.setItems(stationsModel);
        searchToCombo.setItems(stationsModel);
        searchDate.setValue(LocalDate.now());

        seatsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1, 1));

        resTrainColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrainNumber()));
        resFromColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStartStationCity() + " · " + c.getValue().getStartStationName()));
        resDepartureColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDepartureTime() == null ? "" : c.getValue().getDepartureTime().format(UI_FMT)));
        resToColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEndStationCity() + " · " + c.getValue().getEndStationName()));
        resArrivalColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getArrivalTime() == null ? "" : c.getValue().getArrivalTime().format(UI_FMT)));
        resSeatsColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSeatsAvailable()).asObject());
        resStatusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        resultsTable.setItems(resultsModel);

        myIdColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        myTrainColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrainNumber()));
        myFromColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStartStationCity() + " · " + c.getValue().getStartStationName()));
        myToColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEndStationCity() + " · " + c.getValue().getEndStationName()));
        myDepartureColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getScheduleDeparture() == null ? "" : c.getValue().getScheduleDeparture().format(UI_FMT)));
        mySeatsColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSeatsReserved()).asObject());
        myBookedAtColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getBookingDate() == null ? "" : c.getValue().getBookingDate().format(UI_FMT)));
        myBookingsTable.setItems(myBookingsModel);
    }

    public static void show(IService server, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(CustomerController.class.getResource("/customerView.fxml"));
            Parent root = loader.load();

            CustomerController controller = loader.getController();
            controller.setServer(server);
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

    private void loadStations() {
        stationsModel.setAll(server.getAllStations());
    }

    private void loadMyBookings() {
        if (currentUser == null) return;
        myBookingsModel.setAll(server.getBookingsForUser(currentUser.getUsername()));
    }

    @FXML
    public void handleSearch() {
        Station from = searchFromCombo.getValue();
        Station to = searchToCombo.getValue();
        LocalDate date = searchDate.getValue();

        if (from == null || to == null || date == null) {
            setErr(bookingMessageLabel, "Pick from + to + date.");
            return;
        }
        if (from.getId() == to.getId()) {
            setErr(bookingMessageLabel, "From and to must be different stations.");
            return;
        }

        var results = server.searchAvailableSchedules(
                new JourneySearchDTO(from.getId(), to.getId(), date));
        resultsModel.setAll(results);

        if (results.isEmpty()) {
            resultsHint.setText("No trains found for " + from.getStationCity() + " → "
                    + to.getStationCity() + " on " + date + ".");
        } else {
            resultsHint.setText(results.size() + " train(s) on " + date
                    + " from " + from.getStationCity() + " to " + to.getStationCity() + ".");
        }
        bookingMessageLabel.setText("");
    }

    @FXML
    public void handleBook() {
        AvailableScheduleDTO selected = resultsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setErr(bookingMessageLabel, "Pick a train from the results.");
            return;
        }
        Integer seats = seatsSpinner.getValue();
        if (seats == null || seats <= 0) {
            setErr(bookingMessageLabel, "Choose at least 1 seat.");
            return;
        }
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (email.isEmpty()) {
            setErr(bookingMessageLabel, "Enter a confirmation email.");
            return;
        }
        if (seats > selected.getSeatsAvailable()) {
            setErr(bookingMessageLabel,
                    "Only " + selected.getSeatsAvailable() + " seats available on this segment.");
            return;
        }

        try {
            BookingRequestDTO req = new BookingRequestDTO(
                    currentUser.getUsername(),
                    selected.getScheduleId(),
                    selected.getStartStationId(),
                    selected.getEndStationId(),
                    seats,
                    email
            );
            BookingDTO saved = server.bookSeats(req);
            loadMyBookings();
            handleSearch();
            setOk(bookingMessageLabel,
                    "Booking confirmed. Email sent to " + email + ".");
        } catch (AppException e) {
            setErr(bookingMessageLabel, "Booking failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        try {
            server.logoutUser(currentUser.getUsername(), this);
            Stage stage = (Stage) myBookingsTable.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showError("Logout error: " + e.getMessage());
        }
    }

    @Override public void routeAdded(RouteDTO newRoute) {}
    @Override public void routeDeleted(RouteDTO oldRoute) {}
    @Override public void routeUpdated(RouteDTO updatedRoute) {}

    @Override public void stationAdded(StationDTO newStation)   { Platform.runLater(this::loadStations); }
    @Override public void stationDeleted(StationDTO oldStation) { Platform.runLater(this::loadStations); }
    @Override public void stationUpdated(StationDTO updS)       { Platform.runLater(this::loadStations); }

    @Override public void trainAdded(TrainDTO newTrain) {}
    @Override public void trainDeleted(TrainDTO oldTrain) {}
    @Override public void trainUpdated(TrainDTO updatedTrain) {}

    @Override public void scheduleAdded(ScheduleDTO newS)   {}
    @Override public void scheduleDeleted(ScheduleDTO oldS) {}
    @Override public void scheduleUpdated(ScheduleDTO updS) {}

    @Override
    public void bookingAdded(BookingDTO newBooking) {
        Platform.runLater(() -> {
            if (currentUser != null && newBooking.getUsername() != null
                    && newBooking.getUsername().equals(currentUser.getUsername())) {
                loadMyBookings();
            }
        });
    }

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
