package service;

import domain.Train;
import exceptions.AppException;

import java.util.List;

/**
 * CRUD operations for {@link Train} entities. Admin-managed.
 */
public interface ITrainService {

    List<Train> getAllTrains();
    void addTrain(Train train) throws AppException;
    void updateTrain(Train train) throws AppException;
    void removeTrain(Train train) throws AppException;
}
