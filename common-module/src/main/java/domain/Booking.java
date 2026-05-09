package domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "start_station_id", nullable = false)
    private Station startStation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "end_station_id", nullable = false)
    private Station endStation;

    @Column(name = "seats_reserved")
    private int seatsReserved;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    public Booking() {}

    public Booking(User user, Schedule schedule, Station startStation, Station endStation,
                   int seatsReserved, LocalDateTime bookingDate) {
        this.user = user;
        this.schedule = schedule;
        this.startStation = startStation;
        this.endStation = endStation;
        this.seatsReserved = seatsReserved;
        this.bookingDate = bookingDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public Station getStartStation() { return startStation; }
    public void setStartStation(Station startStation) { this.startStation = startStation; }

    public Station getEndStation() { return endStation; }
    public void setEndStation(Station endStation) { this.endStation = endStation; }

    public int getSeatsReserved() { return seatsReserved; }
    public void setSeatsReserved(int seatsReserved) { this.seatsReserved = seatsReserved; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
}
