package dtos;

import java.io.Serializable;
import java.time.LocalDate;

public class JourneySearchDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int startStationId;
    private final int endStationId;
    private final LocalDate date;

    public JourneySearchDTO(int startStationId, int endStationId, LocalDate date) {
        this.startStationId = startStationId;
        this.endStationId = endStationId;
        this.date = date;
    }

    public int getStartStationId() { return startStationId; }
    public int getEndStationId() { return endStationId; }
    public LocalDate getDate() { return date; }
}
