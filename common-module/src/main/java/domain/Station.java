package domain;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "stations")
public class Station implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="station_id")
    private int id;

    @Column(name = "station_name")
    private String stationName;

    @Column(name = "station_city")
    private String stationCity;

    public Station(String stationName, String stationCity) {
        this.stationName = stationName;
        this.stationCity = stationCity;
    }

    public Station() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationCity() {
        return stationCity;
    }

    public void setStationCity(String stationCity) {
        this.stationCity = stationCity;
    }

    @Override
    public String toString() {
        return stationCity + " · " + stationName;
    }
}
