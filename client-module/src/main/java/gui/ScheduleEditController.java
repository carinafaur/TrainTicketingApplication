package gui;

import domain.Route;
import domain.Station;
import domain.Train;
import dtos.ScheduleDTO;
import dtos.ScheduleStopDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScheduleEditController {

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private Label dialogSubtitle;
    @FXML private Label trainLabel;
    @FXML private Label routeLabel;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Spinner<Integer> delaySpinner;
    @FXML private VBox stopsContainer;
    @FXML private Label errorLabel;

    private final ObservableList<Station> allStations = FXCollections.observableArrayList();
    private final List<StopRow> rows = new ArrayList<>();

    private int trainId;
    private String trainNumber;
    private int routeId;
    private int routeStartStationId;
    private String routeStartStationName;
    private int routeEndStationId;
    private String routeEndStationName;

    private Station routeStartStation;
    private Station routeEndStation;

    private boolean loading = false;

    private Integer editingScheduleId;

    private ScheduleDTO result;

    private Stage stage;

    public ScheduleDTO getResult() { return result; }

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList(
                "ON-TIME", "DELAYED", "CANCELLED"));
        statusCombo.getSelectionModel().select("ON-TIME");

        delaySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0, 1));

        delaySpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (loading || newV == null) return;
            String s = statusCombo.getValue();
            if (newV > 0 && "ON-TIME".equals(s)) {
                statusCombo.setValue("DELAYED");
            } else if (newV == 0 && "DELAYED".equals(s)) {
                statusCombo.setValue("ON-TIME");
            }
        });

        statusCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (loading || newV == null) return;
            Integer d = delaySpinner.getValue();
            if (d == null) return;
            if ("DELAYED".equals(newV) && d == 0) {
                delaySpinner.getValueFactory().setValue(1);
            } else if ("ON-TIME".equals(newV) && d > 0) {
                delaySpinner.getValueFactory().setValue(0);
            }
        });

        errorLabel.setText("");
    }

    public void setStations(List<Station> stations) {
        allStations.setAll(stations);
    }

    public void setupForNew(Train train, Route route) {
        this.editingScheduleId = null;
        this.trainId = train.getId();
        this.trainNumber = train.getTrainNumber();
        this.routeId = route.getId();
        this.routeStartStation = route.getStartStation();
        this.routeEndStation = route.getEndStation();
        this.routeStartStationId = routeStartStation.getId();
        this.routeStartStationName = routeStartStation.getStationName();
        this.routeEndStationId = routeEndStation.getId();
        this.routeEndStationName = routeEndStation.getStationName();

        applyMetaLabels();
        rebuildEmptyRows();
    }

    public void setupForEdit(ScheduleDTO existing) {
        this.editingScheduleId = existing.getId();
        this.trainId = existing.getTrainId();
        this.trainNumber = existing.getTrainNumber();
        this.routeId = existing.getRouteId();
        this.routeStartStationId = existing.getRouteStartStationId();
        this.routeStartStationName = existing.getRouteStartStationName();
        this.routeEndStationId = existing.getRouteEndStationId();
        this.routeEndStationName = existing.getRouteEndStationName();

        List<ScheduleStopDTO> stops = existing.getStops();
        if (stops != null && stops.size() >= 2) {
            this.routeStartStation = stationFromStopDTO(stops.get(0));
            this.routeEndStation   = stationFromStopDTO(stops.get(stops.size() - 1));
        } else {
            this.routeStartStation = stationFromName(routeStartStationId, routeStartStationName);
            this.routeEndStation   = stationFromName(routeEndStationId, routeEndStationName);
        }

        applyMetaLabels();

        loading = true;
        try {
            statusCombo.getSelectionModel().select(existing.getStatus() == null ? "ON-TIME" : existing.getStatus());
            delaySpinner.getValueFactory().setValue(existing.getDelayMinutes());
        } finally {
            loading = false;
        }

        rebuildFromExisting(stops);
    }

    private Station stationFromStopDTO(ScheduleStopDTO d) {
        Station s = new Station(d.getStationName(), d.getStationCity());
        s.setId(d.getStationId());
        return s;
    }

    private Station stationFromName(int id, String name) {
        Station s = new Station(name, "");
        s.setId(id);
        return s;
    }

    private void applyMetaLabels() {
        trainLabel.setText(trainNumber == null ? "—" : trainNumber);
        routeLabel.setText(routeStartStationName + " → " + routeEndStationName);
        if (editingScheduleId != null) {
            dialogSubtitle.setText("Editing schedule #" + editingScheduleId);
        } else {
            dialogSubtitle.setText("New schedule for " + trainNumber + " on " + routeStartStationName + " → " + routeEndStationName);
        }
    }

    private void rebuildEmptyRows() {
        rows.clear();
        stopsContainer.getChildren().clear();

        StopRow start = new StopRow(StopKind.START, this.routeStartStation);
        rows.add(start);

        StopRow end = new StopRow(StopKind.END, this.routeEndStation);
        rows.add(end);

        renderRows();
    }

    private void rebuildFromExisting(List<ScheduleStopDTO> stops) {
        rows.clear();
        stopsContainer.getChildren().clear();
        if (stops == null || stops.size() < 2) {
            rebuildEmptyRows();
            return;
        }
        for (int i = 0; i < stops.size(); i++) {
            ScheduleStopDTO sd = stops.get(i);
            StopKind kind;
            Station displayStation;
            if (i == 0) {
                kind = StopKind.START;
                displayStation = this.routeStartStation;
            } else if (i == stops.size() - 1) {
                kind = StopKind.END;
                displayStation = this.routeEndStation;
            } else {
                kind = StopKind.INTERMEDIATE;
                displayStation = findStation(sd.getStationId());
            }

            StopRow row = new StopRow(kind, displayStation);
            row.setArrival(sd.getArrivalTime());
            row.setDeparture(sd.getDepartureTime());
            rows.add(row);
        }
        renderRows();
    }

    private void renderRows() {
        stopsContainer.getChildren().clear();
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setIndex(i);
            stopsContainer.getChildren().add(rows.get(i).asNode());
        }
    }

    @FXML
    public void handleAddIntermediate() {
        int insertAt = rows.size() - 1;
        StopRow row = new StopRow(StopKind.INTERMEDIATE, null);
        rows.add(insertAt, row);
        renderRows();
    }

    private void removeIntermediate(StopRow row) {
        if (row.kind != StopKind.INTERMEDIATE) return;
        rows.remove(row);
        renderRows();
    }

    @FXML
    public void handleSave() {
        try {
            ScheduleDTO dto = collect();
            this.result = dto;
            stage.close();
        } catch (IllegalStateException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        this.result = null;
        stage.close();
    }

    private ScheduleDTO collect() {
        if (rows.size() < 2) {
            throw new IllegalStateException("A schedule needs at least 2 stops.");
        }

        List<ScheduleStopDTO> stopDTOs = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            StopRow r = rows.get(i);

            Station st = r.kind == StopKind.INTERMEDIATE
                    ? r.stationCombo.getValue()
                    : r.fixedStation;

            if (st == null) {
                throw new IllegalStateException("Stop " + (i + 1) + ": pick a station.");
            }

            LocalDateTime arr = null, dep = null;
            if (r.kind != StopKind.START)  arr = parseDateTime(r.arrivalDate.getValue(), r.arrivalTimeField.getText(), "arrival on stop " + (i + 1));
            if (r.kind != StopKind.END)    dep = parseDateTime(r.departureDate.getValue(), r.departureTimeField.getText(), "departure on stop " + (i + 1));

            ScheduleStopDTO s = new ScheduleStopDTO(
                    0,
                    st.getId(),
                    st.getStationName(),
                    st.getStationCity(),
                    i,
                    arr,
                    dep
            );
            stopDTOs.add(s);
        }

        LocalDateTime overallDep = stopDTOs.get(0).getDepartureTime();
        LocalDateTime overallArr = stopDTOs.get(stopDTOs.size() - 1).getArrivalTime();

        return new ScheduleDTO(
                editingScheduleId == null ? 0 : editingScheduleId,
                trainId, trainNumber,
                routeId,
                routeStartStationId, routeStartStationName,
                routeEndStationId, routeEndStationName,
                overallDep, overallArr,
                delaySpinner.getValue(),
                statusCombo.getValue(),
                stopDTOs
        );
    }

    private LocalDateTime parseDateTime(LocalDate d, String hhmm, String field) {
        if (d == null) throw new IllegalStateException("Pick a date for " + field + ".");
        if (hhmm == null || hhmm.trim().isEmpty()) {
            throw new IllegalStateException("Enter HH:mm for " + field + ".");
        }
        try {
            LocalTime t = LocalTime.parse(hhmm.trim(), HHMM);
            return LocalDateTime.of(d, t);
        } catch (Exception e) {
            throw new IllegalStateException(field + ": invalid time format (use HH:mm).");
        }
    }

    private Station findStation(int id) {
        for (Station s : allStations) if (s.getId() == id) return s;
        return null;
    }

    public static ScheduleDTO openForNew(Stage owner,
                                         List<Station> stations,
                                         Train train,
                                         Route route) throws IOException {
        return open(owner, stations, controller -> controller.setupForNew(train, route));
    }

    public static ScheduleDTO openForEdit(Stage owner,
                                          List<Station> stations,
                                          ScheduleDTO existing) throws IOException {
        return open(owner, stations, controller -> controller.setupForEdit(existing));
    }

    private static ScheduleDTO open(Stage owner, List<Station> stations,
                                    Consumer<ScheduleEditController> setup) throws IOException {
        FXMLLoader loader = new FXMLLoader(ScheduleEditController.class.getResource("/scheduleEditDialog.fxml"));
        Parent root = loader.load();
        ScheduleEditController controller = loader.getController();
        controller.setStations(stations);
        setup.accept(controller);

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Schedule");
        stage.setScene(new Scene(root));
        controller.stage = stage;
        stage.showAndWait();
        return controller.getResult();
    }

    private enum StopKind { START, INTERMEDIATE, END }

    private final class StopRow {
        final StopKind kind;
        final Station fixedStation;

        final Label indexLabel = new Label();
        final ComboBox<Station> stationCombo = new ComboBox<>();
        final Label stationLockedLabel = new Label();
        final DatePicker arrivalDate = new DatePicker();
        final TextField arrivalTimeField = new TextField();
        final DatePicker departureDate = new DatePicker();
        final TextField departureTimeField = new TextField();
        final Button removeBtn = new Button("✕");

        StopRow(StopKind kind, Station fixed) {
            this.kind = kind;
            this.fixedStation = fixed;

            stationCombo.setItems(allStations);
            stationCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Station s) { return s == null ? "" : s.getStationCity() + " · " + s.getStationName(); }
                @Override public Station fromString(String s) { return null; }
            });
            stationCombo.setPrefWidth(220);

            arrivalDate.setPrefWidth(130);
            arrivalTimeField.setPrefWidth(80);
            arrivalTimeField.setPromptText("HH:mm");
            departureDate.setPrefWidth(130);
            departureTimeField.setPrefWidth(80);
            departureTimeField.setPromptText("HH:mm");

            removeBtn.getStyleClass().addAll("btn", "btn-danger");
            removeBtn.setOnAction(e -> removeIntermediate(this));

            if (kind == StopKind.START) {
                stationLockedLabel.setText(fixed != null ? fixed.getStationCity() + " · " + fixed.getStationName() : "(start)");
                stationLockedLabel.getStyleClass().add("locked-station");
                arrivalDate.setDisable(true);
                arrivalTimeField.setDisable(true);
                arrivalTimeField.setPromptText("—");
                removeBtn.setDisable(true);
                removeBtn.setVisible(false);
            } else if (kind == StopKind.END) {
                stationLockedLabel.setText(fixed != null ? fixed.getStationCity() + " · " + fixed.getStationName() : "(end)");
                stationLockedLabel.getStyleClass().add("locked-station");
                departureDate.setDisable(true);
                departureTimeField.setDisable(true);
                departureTimeField.setPromptText("—");
                removeBtn.setDisable(true);
                removeBtn.setVisible(false);
            } else {
                if (fixed != null) {
                    stationCombo.getSelectionModel().select(fixed);
                }
            }
        }

        void setIndex(int i) {
            indexLabel.setText(String.valueOf(i + 1));
            indexLabel.setPrefWidth(30);
        }

        void setArrival(LocalDateTime t) {
            if (t == null) return;
            arrivalDate.setValue(t.toLocalDate());
            arrivalTimeField.setText(HHMM.format(t.toLocalTime()));
        }

        void setDeparture(LocalDateTime t) {
            if (t == null) return;
            departureDate.setValue(t.toLocalDate());
            departureTimeField.setText(HHMM.format(t.toLocalTime()));
        }

        HBox asNode() {
            HBox box = new HBox(8);
            box.setAlignment(Pos.CENTER_LEFT);

            javafx.scene.Node stationCell;
            if (kind == StopKind.INTERMEDIATE) {
                stationCell = stationCombo;
            } else {
                stationLockedLabel.setPrefWidth(220);
                stationCell = stationLockedLabel;
            }

            box.getChildren().addAll(
                    indexLabel,
                    stationCell,
                    arrivalDate,
                    arrivalTimeField,
                    departureDate,
                    departureTimeField,
                    removeBtn
            );
            return box;
        }
    }
}
