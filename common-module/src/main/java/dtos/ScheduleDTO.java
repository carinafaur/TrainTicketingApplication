package dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;

    private int trainId;
    private String trainNumber;

    private int routeId;
    private int routeStartStationId;
    private String routeStartStationName;
    private int routeEndStationId;
    private String routeEndStationName;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int delayMinutes;
    private String status;

    private List<ScheduleStopDTO> stops = new ArrayList<>();

    public ScheduleDTO() {}

    public ScheduleDTO(int id,
                       int trainId, String trainNumber,
                       int routeId,
                       int routeStartStationId, String routeStartStationName,
                       int routeEndStationId, String routeEndStationName,
                       LocalDateTime departureTime, LocalDateTime arrivalTime,
                       int delayMinutes, String status,
                       List<ScheduleStopDTO> stops) {
        this.id = id;
        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.routeId = routeId;
        this.routeStartStationId = routeStartStationId;
        this.routeStartStationName = routeStartStationName;
        this.routeEndStationId = routeEndStationId;
        this.routeEndStationName = routeEndStationName;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.delayMinutes = delayMinutes;
        this.status = status;
        this.stops = stops != null ? stops : new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTrainId() { return trainId; }
    public void setTrainId(int trainId) { this.trainId = trainId; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }

    public int getRouteStartStationId() { return routeStartStationId; }
    public void setRouteStartStationId(int routeStartStationId) { this.routeStartStationId = routeStartStationId; }

    public String getRouteStartStationName() { return routeStartStationName; }
    public void setRouteStartStationName(String routeStartStationName) { this.routeStartStationName = routeStartStationName; }

    public int getRouteEndStationId() { return routeEndStationId; }
    public void setRouteEndStationId(int routeEndStationId) { this.routeEndStationId = routeEndStationId; }

    public String getRouteEndStationName() { return routeEndStationName; }
    public void setRouteEndStationName(String routeEndStationName) { this.routeEndStationName = routeEndStationName; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ScheduleStopDTO> getStops() { return stops; }
    public void setStops(List<ScheduleStopDTO> stops) { this.stops = stops; }

    public String getRouteLabel() {
        return routeStartStationName + " → " + routeEndStationName;
    }
}
