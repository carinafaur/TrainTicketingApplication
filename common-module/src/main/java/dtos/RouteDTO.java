package dtos;

import java.io.Serializable;

public class RouteDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String startStationName;
    private String destinationStationName;

    private int startStationId;
    private int destinationStationId;

    public RouteDTO(int id, String startStationName, String destinationStationName, int startStationId, int destinationStationId) {
        this.id = id;
        this.startStationName = startStationName;
        this.destinationStationName = destinationStationName;
        this.startStationId = startStationId;
        this.destinationStationId = destinationStationId;
    }

    public int getId() { return id; }
    public String getStartStationName() { return startStationName; }
    public String getDestinationStationName() { return destinationStationName; }
    public int getStartStationId() { return startStationId; }
    public int getDestinationStationId() { return destinationStationId; }
}