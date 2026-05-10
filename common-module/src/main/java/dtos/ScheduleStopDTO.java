package dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ScheduleStopDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int stationId;
    private String stationName;
    private String stationCity;
    private int stopOrder;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;

    public ScheduleStopDTO() {}

    public ScheduleStopDTO(int id, int stationId, String stationName, String stationCity,
                           int stopOrder, LocalDateTime arrivalTime, LocalDateTime departureTime) {
        this.id = id;
        this.stationId = stationId;
        this.stationName = stationName;
        this.stationCity = stationCity;
        this.stopOrder = stopOrder;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getStationCity() { return stationCity; }
    public void setStationCity(String stationCity) { this.stationCity = stationCity; }

    public int getStopOrder() { return stopOrder; }
    public void setStopOrder(int stopOrder) { this.stopOrder = stopOrder; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
}
