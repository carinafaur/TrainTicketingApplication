package service;

import domain.Station;
import exceptions.AlreadyExistsException;
import exceptions.NotFoundException;
import exceptions.ValidationException;
import repository.StationRepository;
import validators.StationValidator;

import java.util.List;

public class StationService {
    private final StationRepository stationRepository;
    private final StationValidator stationValidator;

    public StationService(StationRepository stationRepository, StationValidator stationValidator) {
        this.stationRepository = stationRepository;
        this.stationValidator = stationValidator;
    }

    public Station findStationById(int id) {
        return stationRepository.findStationById(id);
    }

    public List<Station> getAllStations() {
        return stationRepository.getAllStations();
    }

    public Station addStation(Station station) throws ValidationException, AlreadyExistsException {
        stationValidator.validate(station);
        Station existing = stationRepository.findByNameAndCity(
                station.getStationName(), station.getStationCity());
        if (existing != null) {
            throw new AlreadyExistsException(
                    "A station '" + station.getStationName() + "' in '" + station.getStationCity() + "' already exists.");
        }
        return stationRepository.save(station);
    }

    public Station updateStation(Station station) throws ValidationException, NotFoundException, AlreadyExistsException {
        stationValidator.validate(station);
        Station byId = stationRepository.findStationById(station.getId());
        if (byId == null) {
            throw new NotFoundException("Station with id " + station.getId() + " does not exist.");
        }
        Station byNameCity = stationRepository.findByNameAndCity(
                station.getStationName(), station.getStationCity());
        if (byNameCity != null && byNameCity.getId() != station.getId()) {
            throw new AlreadyExistsException(
                    "Another station '" + station.getStationName() + "' in '" + station.getStationCity() + "' already exists.");
        }
        return stationRepository.update(station);
    }

    public Station deleteStation(Station station) throws NotFoundException {
        Station existing = stationRepository.findStationById(station.getId());
        if (existing == null) {
            throw new NotFoundException("Station with id " + station.getId() + " does not exist.");
        }
        return stationRepository.delete(existing);
    }
}
