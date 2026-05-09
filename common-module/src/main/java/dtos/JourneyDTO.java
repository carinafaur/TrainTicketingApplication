package dtos;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class JourneyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<JourneyLegDTO> legs;

    public JourneyDTO(List<JourneyLegDTO> legs) {
        this.legs = legs;
    }

    public List<JourneyLegDTO> getLegs() { return legs; }

    public int getNumberOfChangeovers() {
        return Math.max(0, legs.size() - 1);
    }

    public boolean isDirect() {
        return legs.size() == 1;
    }

    public LocalDateTime getOverallDeparture() {
        return legs.isEmpty() ? null : legs.get(0).getDepartureTime();
    }

    public LocalDateTime getOverallArrival() {
        return legs.isEmpty() ? null : legs.getLast().getArrivalTime();
    }

    public long getTotalDurationMinutes() {
        if (getOverallDeparture() == null || getOverallArrival() == null) return 0;
        return Duration.between(getOverallDeparture(), getOverallArrival()).toMinutes();
    }

    public String getTrainsLabel() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < legs.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(legs.get(i).getTrainNumber());
        }
        return sb.toString();
    }

    public int getMinSeatsAvailable() {
        int min = Integer.MAX_VALUE;
        for (JourneyLegDTO l : legs) {
            if (l.getSeatsAvailable() < min) min = l.getSeatsAvailable();
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }
}
