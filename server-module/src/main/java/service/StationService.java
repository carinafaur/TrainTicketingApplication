package service;

import domain.Station;
import repository.StationRepository;
import java.util.List;

public class StationService {
    private StationRepository stationRepository;
    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public Station findStationById(int id) {
        return stationRepository.findStationById(id);
    }

    public List<Station> getAllStations() {
        return stationRepository.getAllStations();
    }
}
