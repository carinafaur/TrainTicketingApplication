package service;

import domain.Schedule;
import domain.ScheduleStop;
import exceptions.NotFoundException;
import exceptions.ValidationException;
import repository.ScheduleRepository;
import validators.ScheduleValidator;

import java.util.List;

public class ScheduleService {
    private final ScheduleRepository repo;
    private final ScheduleValidator validator;

    public ScheduleService(ScheduleRepository repo, ScheduleValidator validator) {
        this.repo = repo;
        this.validator = validator;
    }

    public List<Schedule> getAllSchedules() {
        return repo.getAllSchedules();
    }

    public Schedule findById(int id) {
        return repo.findById(id);
    }

    public Schedule addSchedule(Schedule schedule) throws ValidationException {
        normalize(schedule);
        validator.validate(schedule);
        return repo.save(schedule);
    }

    public Schedule updateSchedule(Schedule schedule) throws ValidationException, NotFoundException {
        Schedule existing = repo.findById(schedule.getId());
        if (existing == null) {
            throw new NotFoundException("Schedule with id " + schedule.getId() + " does not exist.");
        }
        normalize(schedule);
        validator.validate(schedule);
        return repo.update(schedule);
    }

    public Schedule deleteSchedule(Schedule schedule) throws NotFoundException {
        Schedule existing = repo.findById(schedule.getId());
        if (existing == null) {
            throw new NotFoundException("Schedule with id " + schedule.getId() + " does not exist.");
        }
        return repo.delete(existing);
    }

    private void normalize(Schedule s) {
        List<ScheduleStop> stops = s.getStops();
        if (stops == null || stops.isEmpty()) return;
        for (int i = 0; i < stops.size(); i++) {
            stops.get(i).setStopOrder(i);
        }
        s.setDepartureTime(stops.getFirst().getDepartureTime());
        s.setArrivalTime(stops.getLast().getArrivalTime());
    }
}
