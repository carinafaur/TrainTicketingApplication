package dtos;

import java.io.Serializable;

public class StationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String stationName;
    private String stationCity;

    public StationDTO() {}

    public StationDTO(int id, String stationName, String stationCity) {
        this.id = id;
        this.stationName = stationName;
        this.stationCity = stationCity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getStationCity() { return stationCity; }
    public void setStationCity(String stationCity) { this.stationCity = stationCity; }

    @Override
    public String toString() {
        return stationCity + " · " + stationName;
    }
}
