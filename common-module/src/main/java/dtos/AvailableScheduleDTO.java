package dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AvailableScheduleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int scheduleId;
    private int trainId;
    private String trainNumber;
    private int trainCapacity;

    private int startStationId;
    private String startStationName;
    private String startStationCity;
    private LocalDateTime departureTime;

    private int endStationId;
    private String endStationName;
    private String endStationCity;
    private LocalDateTime arrivalTime;

    private int delayMinutes;
    private String status;

    private int seatsAvailable;

    public AvailableScheduleDTO() {}

    public AvailableScheduleDTO(int scheduleId, int trainId, String trainNumber, int trainCapacity,
                                int startStationId, String startStationName, String startStationCity,
                                LocalDateTime departureTime,
                                int endStationId, String endStationName, String endStationCity,
                                LocalDateTime arrivalTime,
                                int delayMinutes, String status, int seatsAvailable) {
        this.scheduleId = scheduleId;
        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.trainCapacity = trainCapacity;
        this.startStationId = startStationId;
        this.startStationName = startStationName;
        this.startStationCity = startStationCity;
        this.departureTime = departureTime;
        this.endStationId = endStationId;
        this.endStationName = endStationName;
        this.endStationCity = endStationCity;
        this.arrivalTime = arrivalTime;
        this.delayMinutes = delayMinutes;
        this.status = status;
        this.seatsAvailable = seatsAvailable;
    }

    public int getScheduleId() { return scheduleId; }
    public int getTrainId() { return trainId; }
    public String getTrainNumber() { return trainNumber; }
    public int getTrainCapacity() { return trainCapacity; }
    public int getStartStationId() { return startStationId; }
    public String getStartStationName() { return startStationName; }
    public String getStartStationCity() { return startStationCity; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getEndStationId() { return endStationId; }
    public String getEndStationName() { return endStationName; }
    public String getEndStationCity() { return endStationCity; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public int getDelayMinutes() { return delayMinutes; }
    public String getStatus() { return status; }
    public int getSeatsAvailable() { return seatsAvailable; }
}
