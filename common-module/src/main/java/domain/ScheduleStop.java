package domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_stops")
public class ScheduleStop implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stop_id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "stop_order")
    private int stopOrder;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    public ScheduleStop() {}

    public ScheduleStop(Station station, int stopOrder,
                        LocalDateTime arrivalTime, LocalDateTime departureTime) {
        this.station = station;
        this.stopOrder = stopOrder;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }

    public int getStopOrder() { return stopOrder; }
    public void setStopOrder(int stopOrder) { this.stopOrder = stopOrder; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
}
