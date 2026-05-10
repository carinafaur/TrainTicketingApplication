package gui;

import domain.Station;
import domain.User;
import dtos.AvailableScheduleDTO;
import dtos.BookingDTO;
import dtos.BookingRequestDTO;
import dtos.JourneyDTO;
import dtos.JourneyLegDTO;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import service.IAuthService;
import service.IBookingService;
import service.IObserver;
import service.IService;
import service.IStationService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerController extends BaseController implements IObserver {

    private IAuthService auth;
    private IStationService stationApi;
    private IBookingService bookingApi;
    private User currentUser;

    private final ObservableList<Station> stationsModel = FXCollections.observableArrayList();
    private final ObservableList<AvailableScheduleDTO> resultsModel = FXCollections.observableArrayList();
    private final ObservableList<BookingDTO> myBookingsModel = FXCollections.observableArrayList();
    private final ObservableList<JourneyDTO> journeysModel = FXCollections.observableArrayList();

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

    @FXML private ComboBox<Station> connFromCombo;
    @FXML private ComboBox<Station> connToCombo;
    @FXML private DatePicker connDate;
    @FXML private Label connHint;
    @FXML private TableView<JourneyDTO> journeysTable;
    @FXML private TableColumn<JourneyDTO, String>  jDepartureColumn;
    @FXML private TableColumn<JourneyDTO, String>  jArrivalColumn;
    @FXML private TableColumn<JourneyDTO, String>  jDurationColumn;
    @FXML private TableColumn<JourneyDTO, Integer> jChangesColumn;
    @FXML private TableColumn<JourneyDTO, String>  jTrainsColumn;
    @FXML private TableColumn<JourneyDTO, Integer> jSeatsColumn;
    @FXML private TextArea itineraryArea;

    public void setServer(IService server) {
        this.auth = server;
        this.stationApi = server;
        this.bookingApi = server;
        auth.setObserver(this);
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

        connFromCombo.setConverter(stationConverter);
        connToCombo.setConverter(stationConverter);
        connFromCombo.setItems(stationsModel);
        connToCombo.setItems(stationsModel);
        connDate.setValue(LocalDate.now());

        jDepartureColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getOverallDeparture() == null ? "" : c.getValue().getOverallDeparture().format(UI_FMT)));
        jArrivalColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getOverallArrival() == null ? "" : c.getValue().getOverallArrival().format(UI_FMT)));
        jDurationColumn.setCellValueFactory(c -> new SimpleStringProperty(formatDuration(c.getValue().getTotalDurationMinutes())));
        jChangesColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumberOfChangeovers()).asObject());
        jTrainsColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrainsLabel()));
        jSeatsColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMinSeatsAvailable()).asObject());
        journeysTable.setItems(journeysModel);

        journeysTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            itineraryArea.setText(formatItinerary(newV));
        });
    }

    public static void show(IService server, User user) {
        CustomerController controller = loadFxml("/customerView.fxml");
        controller.setServer(server);
        controller.setUser(user);
        controller.showInNewStage("Train Ticketing — Customer · " + user.getUsername());
    }

    private void loadStations() {
        stationsModel.setAll(stationApi.getAllStations());
    }

    private void loadMyBookings() {
        if (currentUser == null) return;
        myBookingsModel.setAll(bookingApi.getBookingsForUser(currentUser.getUsername()));
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

        var results = bookingApi.searchAvailableSchedules(
                new JourneySearchDTO(from.getId(), to.getId(), date));
        resultsModel.setAll(results);

        if (results.isEmpty()) {
            resultsHint.setText("No direct train found for " + from.getStationCity() + " → "
                    + to.getStationCity() + " on " + date + ". Check the 'Find connections' tab for journeys with changeovers.");
        } else {
            resultsHint.setText(results.size() + " direct train(s) on " + date
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
            BookingDTO saved = bookingApi.bookSeats(req);
            loadMyBookings();
            handleSearch();
            setOk(bookingMessageLabel,
                    "Booking #" + saved.getId() + " confirmed. Email sent to " + email + ".");
        } catch (AppException e) {
            setErr(bookingMessageLabel, "Booking failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearchJourneys() {
        Station from = connFromCombo.getValue();
        Station to = connToCombo.getValue();
        LocalDate date = connDate.getValue();

        if (from == null || to == null || date == null) {
            connHint.setText("Pick from + to + date.");
            journeysModel.clear();
            itineraryArea.setText("");
            return;
        }
        if (from.getId() == to.getId()) {
            connHint.setText("From and to must be different stations.");
            journeysModel.clear();
            itineraryArea.setText("");
            return;
        }

        var journeys = bookingApi.searchJourneys(new JourneySearchDTO(from.getId(), to.getId(), date));
        journeysModel.setAll(journeys);

        if (journeys.isEmpty()) {
            connHint.setText("No journey found between " + from.getStationCity()
                    + " and " + to.getStationCity() + " on " + date
                    + ". The stations are not linked, even with one changeover.");
            itineraryArea.setText("");
        } else {
            long direct = journeys.stream().filter(JourneyDTO::isDirect).count();
            long indirect = journeys.size() - direct;
            connHint.setText(journeys.size() + " journey(s) found — "
                    + direct + " direct, " + indirect + " with one changeover. Click a row to see the itinerary.");
            journeysTable.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void handleLogout() {
        try {
            auth.logoutUser(currentUser.getUsername(), this);
            closeWindowOf(myBookingsTable);
        } catch (Exception e) {
            showError("Logout error: " + e.getMessage());
        }
    }

    private String formatDuration(long minutes) {
        if (minutes <= 0) return "—";
        long h = minutes / 60;
        long m = minutes % 60;
        if (h == 0) return m + "m";
        return h + "h " + (m == 0 ? "" : m + "m");
    }

    private String formatItinerary(JourneyDTO journey) {
        if (journey == null || journey.getLegs() == null || journey.getLegs().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < journey.getLegs().size(); i++) {
            JourneyLegDTO leg = journey.getLegs().get(i);
            sb.append("Leg ").append(i + 1).append(" — ").append(leg.getTrainNumber()).append('\n');
            sb.append("    ").append(formatTime(leg.getDepartureTime()))
                    .append("  ").append(leg.getFromStationCity()).append(" · ").append(leg.getFromStationName()).append('\n');
            sb.append("    ").append(formatTime(leg.getArrivalTime()))
                    .append("  ").append(leg.getToStationCity()).append(" · ").append(leg.getToStationName()).append('\n');
            sb.append("    Seats free on this leg: ").append(leg.getSeatsAvailable()).append('\n');
            if (i < journey.getLegs().size() - 1) {
                JourneyLegDTO next = journey.getLegs().get(i + 1);
                long layover = Duration.between(leg.getArrivalTime(), next.getDepartureTime()).toMinutes();
                sb.append("    Changeover at ").append(leg.getToStationCity())
                        .append(" — wait ").append(layover).append(" min").append("\n\n");
            }
        }
        return sb.toString();
    }

    private String formatTime(LocalDateTime t) {
        return t == null ? "—  " : t.format(UI_FMT);
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
}
