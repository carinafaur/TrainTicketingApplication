package dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

public class JourneyLegDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int scheduleId;
    private int trainId;
    private String trainNumber;

    private int fromStationId;
    private String fromStationName;
    private String fromStationCity;
    private LocalDateTime departureTime;

    private int toStationId;
    private String toStationName;
    private String toStationCity;
    private LocalDateTime arrivalTime;

    private int seatsAvailable;
    private int delayMinutes;
    private String status;

    public JourneyLegDTO() {}

    public JourneyLegDTO(int scheduleId, int trainId, String trainNumber,
                         int fromStationId, String fromStationName, String fromStationCity,
                         LocalDateTime departureTime,
                         int toStationId, String toStationName, String toStationCity,
                         LocalDateTime arrivalTime,
                         int seatsAvailable, int delayMinutes, String status) {
        this.scheduleId = scheduleId;
        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.fromStationId = fromStationId;
        this.fromStationName = fromStationName;
        this.fromStationCity = fromStationCity;
        this.departureTime = departureTime;
        this.toStationId = toStationId;
        this.toStationName = toStationName;
        this.toStationCity = toStationCity;
        this.arrivalTime = arrivalTime;
        this.seatsAvailable = seatsAvailable;
        this.delayMinutes = delayMinutes;
        this.status = status;
    }

    public int getScheduleId() { return scheduleId; }
    public int getTrainId() { return trainId; }
    public String getTrainNumber() { return trainNumber; }
    public int getFromStationId() { return fromStationId; }
    public String getFromStationName() { return fromStationName; }
    public String getFromStationCity() { return fromStationCity; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getToStationId() { return toStationId; }
    public String getToStationName() { return toStationName; }
    public String getToStationCity() { return toStationCity; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public int getSeatsAvailable() { return seatsAvailable; }
    public int getDelayMinutes() { return delayMinutes; }
    public String getStatus() { return status; }
}
