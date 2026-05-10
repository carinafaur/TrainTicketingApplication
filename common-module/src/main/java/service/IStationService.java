package service;

import domain.Station;
import exceptions.AppException;

import java.util.List;

/**
 * CRUD operations for {@link Station} entities. Read-only access is what
 * customers need ({@link #getAllStations()}); the rest is admin-managed.
 */
public interface IStationService {

    List<Station> getAllStations();
    Station findStationById(int id);
    void addStation(Station station) throws AppException;
    void updateStation(Station station) throws AppException;
    void removeStation(Station station) throws AppException;
}
