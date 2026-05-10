package service;

import domain.Schedule;
import exceptions.AppException;

import java.util.List;

/**
 * CRUD operations for {@link Schedule} entities and their stops. Admin-managed.
 */
public interface IScheduleService {

    List<Schedule> getAllSchedules();
    void addSchedule(Schedule schedule) throws AppException;
    void updateSchedule(Schedule schedule) throws AppException;
    void removeSchedule(Schedule schedule) throws AppException;
}
