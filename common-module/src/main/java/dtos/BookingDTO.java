package dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BookingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;

    private int userId;
    private String username;

    private int scheduleId;
    private String trainNumber;
    private LocalDateTime scheduleDeparture;
    private LocalDateTime scheduleArrival;

    private int startStationId;
    private String startStationName;
    private String startStationCity;

    private int endStationId;
    private String endStationName;
    private String endStationCity;

    private int seatsReserved;
    private LocalDateTime bookingDate;

    public BookingDTO() {}

    public BookingDTO(int id, int userId, String username,
                      int scheduleId, String trainNumber,
                      LocalDateTime scheduleDeparture, LocalDateTime scheduleArrival,
                      int startStationId, String startStationName, String startStationCity,
                      int endStationId, String endStationName, String endStationCity,
                      int seatsReserved, LocalDateTime bookingDate) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.scheduleId = scheduleId;
        this.trainNumber = trainNumber;
        this.scheduleDeparture = scheduleDeparture;
        this.scheduleArrival = scheduleArrival;
        this.startStationId = startStationId;
        this.startStationName = startStationName;
        this.startStationCity = startStationCity;
        this.endStationId = endStationId;
        this.endStationName = endStationName;
        this.endStationCity = endStationCity;
        this.seatsReserved = seatsReserved;
        this.bookingDate = bookingDate;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getScheduleId() { return scheduleId; }
    public String getTrainNumber() { return trainNumber; }
    public LocalDateTime getScheduleDeparture() { return scheduleDeparture; }
    public LocalDateTime getScheduleArrival() { return scheduleArrival; }
    public int getStartStationId() { return startStationId; }
    public String getStartStationName() { return startStationName; }
    public String getStartStationCity() { return startStationCity; }
    public int getEndStationId() { return endStationId; }
    public String getEndStationName() { return endStationName; }
    public String getEndStationCity() { return endStationCity; }
    public int getSeatsReserved() { return seatsReserved; }
    public LocalDateTime getBookingDate() { return bookingDate; }

    public String getJourneyLabel() {
        return startStationCity + " → " + endStationCity;
    }
}
