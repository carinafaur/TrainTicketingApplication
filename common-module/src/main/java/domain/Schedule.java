package domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedules")
public class Schedule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "train_id")
    private Train train;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "delay_minutes")
    private int delayMinutes;

    @Column(name = "status", length = 20)
    private String status;

    @OneToMany(
            mappedBy = "schedule",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @OrderBy("stopOrder ASC")
    private List<ScheduleStop> stops = new ArrayList<>();

    public Schedule() {}

    public Schedule(Train train, Route route,
                    LocalDateTime departureTime, LocalDateTime arrivalTime,
                    int delayMinutes, String status) {
        this.train = train;
        this.route = route;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.delayMinutes = delayMinutes;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ScheduleStop> getStops() { return stops; }
    public void setStops(List<ScheduleStop> stops) { this.stops = stops; }

    public void addStop(ScheduleStop stop) {
        stop.setSchedule(this);
        this.stops.add(stop);
    }

    public void replaceStops(List<ScheduleStop> newStops) {
        this.stops.clear();
        for (ScheduleStop s : newStops) {
            addStop(s);
        }
    }
}
