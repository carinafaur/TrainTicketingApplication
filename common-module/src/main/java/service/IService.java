package service;

import domain.Route;
import domain.Schedule;
import domain.Station;
import domain.Train;
import domain.User;
import dtos.AvailableScheduleDTO;
import dtos.BookingDTO;
import dtos.BookingRequestDTO;
import dtos.JourneySearchDTO;
import exceptions.AppException;

import java.util.List;

/**
 * Application-level facade for all client operations. Implemented on the
 * server side by {@code MasterService} and on the client side by
 * {@code ServerProxy}.
 */
public interface IService {

    User loginUser(String username, String password, IObserver client) throws AppException;
    void logoutUser(String username, IObserver client);
    void setObserver(IObserver clientObserver);

    List<Route> getAllRoutes();
    void addRoute(Route route) throws AppException;
    void removeRoute(Route route) throws AppException;
    void updateRoute(Route newRoute) throws AppException;

    List<Station> getAllStations();
    Station findStationById(int id);
    void addStation(Station station) throws AppException;
    void updateStation(Station station) throws AppException;
    void removeStation(Station station) throws AppException;

    List<Train> getAllTrains();
    void addTrain(Train train) throws AppException;
    void updateTrain(Train train) throws AppException;
    void removeTrain(Train train) throws AppException;

    List<Schedule> getAllSchedules();
    void addSchedule(Schedule schedule) throws AppException;
    void updateSchedule(Schedule schedule) throws AppException;
    void removeSchedule(Schedule schedule) throws AppException;

    List<AvailableScheduleDTO> searchAvailableSchedules(JourneySearchDTO criteria);
    BookingDTO bookSeats(BookingRequestDTO request) throws AppException;
    List<BookingDTO> getAllBookings();
    List<BookingDTO> getBookingsForUser(String username);
}
