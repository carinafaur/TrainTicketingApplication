package service;

import domain.Train;
import exceptions.AlreadyExistsException;
import exceptions.NotFoundException;
import exceptions.ValidationException;
import repository.TrainRepository;
import validators.TrainValidator;

import java.util.List;

public class TrainService {
    private final TrainRepository trainRepository;
    private final TrainValidator trainValidator;

    public TrainService(TrainRepository trainRepository, TrainValidator trainValidator) {
        this.trainRepository = trainRepository;
        this.trainValidator = trainValidator;
    }

    public List<Train> getAllTrains() {
        return trainRepository.getAllTrains();
    }

    public Train addTrain(Train train) throws ValidationException, AlreadyExistsException {
        trainValidator.validate(train);
        Train existing = trainRepository.findByTrainNumber(train.getTrainNumber());
        if (existing != null) {
            throw new AlreadyExistsException("A train with number " + train.getTrainNumber() + " already exists.");
        }
        return trainRepository.save(train);
    }

    public Train updateTrain(Train train) throws ValidationException, NotFoundException, AlreadyExistsException {
        trainValidator.validate(train);
        Train byId = trainRepository.findById(train.getId());
        if (byId == null) {
            throw new NotFoundException("Train with id " + train.getId() + " does not exist.");
        }
        Train byNumber = trainRepository.findByTrainNumber(train.getTrainNumber());
        if (byNumber != null && byNumber.getId() != train.getId()) {
            throw new AlreadyExistsException("Another train with number " + train.getTrainNumber() + " already exists.");
        }
        return trainRepository.update(train);
    }

    public Train deleteTrain(Train train) throws NotFoundException {
        Train existing = trainRepository.findById(train.getId());
        if (existing == null) {
            throw new NotFoundException("Train with id " + train.getId() + " does not exist.");
        }
        return trainRepository.delete(existing);
    }
}
