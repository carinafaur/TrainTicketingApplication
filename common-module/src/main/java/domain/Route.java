package domain;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name="routes")
public class Route implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="route_id")
    private int id;

    @ManyToOne
    @JoinColumn(name="start_station")
    private Station startStation;

    @ManyToOne
    @JoinColumn(name="end_station")
    private Station endStation;

    public Route(Station startStation, Station endStation) {
        this.startStation = startStation;
        this.endStation = endStation;
    }

    public Route() {}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Station getStartStation() {
        return startStation;
    }
    public void setStartStation(Station startStation) {
        this.startStation = startStation;
    }

    public Station getEndStation() {
        return endStation;
    }
    public void setEndStation(Station endStation) {
        this.endStation = endStation;
    }

    @Transient
    public String getStartCity(){
        return startStation.getStationCity();
    }

    @Transient
    public String getEndCity(){
        return endStation.getStationCity();
    }
}
