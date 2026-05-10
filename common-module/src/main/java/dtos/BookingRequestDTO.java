package dtos;

import java.io.Serializable;

public class BookingRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final int scheduleId;
    private final int startStationId;
    private final int endStationId;
    private final int seatsReserved;
    private final String passengerEmail;

    public BookingRequestDTO(String username, int scheduleId,
                             int startStationId, int endStationId,
                             int seatsReserved, String passengerEmail) {
        this.username = username;
        this.scheduleId = scheduleId;
        this.startStationId = startStationId;
        this.endStationId = endStationId;
        this.seatsReserved = seatsReserved;
        this.passengerEmail = passengerEmail;
    }

    public String getUsername() { return username; }
    public int getScheduleId() { return scheduleId; }
    public int getStartStationId() { return startStationId; }
    public int getEndStationId() { return endStationId; }
    public int getSeatsReserved() { return seatsReserved; }
    public String getPassengerEmail() { return passengerEmail; }
}
