package service;

import domain.Booking;
import domain.Route;
import domain.Schedule;
import domain.Station;
import domain.Train;
import domain.User;
import dtos.AvailableScheduleDTO;
import dtos.BookingDTO;
import dtos.BookingRequestDTO;
import dtos.DTOUtils;
import dtos.JourneyDTO;
import dtos.JourneySearchDTO;
import dtos.RegisterRequestDTO;
import exceptions.AlreadyExistsException;
import exceptions.AppException;
import exceptions.NotFoundException;
import exceptions.ValidationException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MasterService implements IService {
    private final UserService userService;
    private final RouteService routeService;
    private final StationService stationService;
    private final TrainService trainService;
    private final ScheduleService scheduleService;
    private final BookingService bookingService;
    private final JourneySearchService journeySearchService;
    private final Map<String, IObserver> loggedClients = new ConcurrentHashMap<>();

    public MasterService(UserService userService,
                         RouteService routeService,
                         StationService stationService,
                         TrainService trainService,
                         ScheduleService scheduleService,
                         BookingService bookingService,
                         JourneySearchService journeySearchService) {
        this.userService = userService;
        this.routeService = routeService;
        this.stationService = stationService;
        this.trainService = trainService;
        this.scheduleService = scheduleService;
        this.bookingService = bookingService;
        this.journeySearchService = journeySearchService;
    }

    @Override
    public User loginUser(String username, String password, IObserver client) throws NotFoundException, AlreadyExistsException {
        User user = userService.loginUser(username, password);
        if (user != null) {
            if (loggedClients.containsKey(user.getUsername())) {
                throw new AlreadyExistsException("User is already logged in!");
            }
            loggedClients.put(user.getUsername(), client);
        }
        return user;
    }

    @Override
    public void setObserver(IObserver clientObserver) { }

    @Override
    public void logoutUser(String username, IObserver client) {
        loggedClients.remove(username);
    }

    @Override
    public User registerUser(RegisterRequestDTO request, IObserver client) throws AppException {
        User user = userService.register(request);
        loggedClients.put(user.getUsername(), client);
        return user;
    }

    @Override
    public List<Route> getAllRoutes() { return routeService.getAllRoutes(); }

    @Override
    public void addRoute(Route route) throws ValidationException, AlreadyExistsException {
        Route added = routeService.addRoute(route);
        loggedClients.forEach((k, obs) -> obs.routeAdded(DTOUtils.getDTO(added)));
    }

    @Override
    public void updateRoute(Route newRoute) throws AppException {
        Route updated = routeService.updateRoute(newRoute);
        loggedClients.forEach((k, obs) -> obs.routeUpdated(DTOUtils.getDTO(updated)));
    }

    @Override
    public void removeRoute(Route route) throws AppException {
        routeService.deleteRoute(route);
        loggedClients.forEach((k, obs) -> obs.routeDeleted(DTOUtils.getDTO(route)));
    }

    @Override
    public List<Station> getAllStations() { return stationService.getAllStations(); }

    @Override
    public Station findStationById(int id) { return stationService.findStationById(id); }

    @Override
    public void addStation(Station station) throws AppException {
        Station added = stationService.addStation(station);
        loggedClients.forEach((k, obs) -> obs.stationAdded(DTOUtils.getDTO(added)));
    }

    @Override
    public void updateStation(Station station) throws AppException {
        Station updated = stationService.updateStation(station);
        loggedClients.forEach((k, obs) -> obs.stationUpdated(DTOUtils.getDTO(updated)));
    }

    @Override
    public void removeStation(Station station) throws AppException {
        stationService.deleteStation(station);
        loggedClients.forEach((k, obs) -> obs.stationDeleted(DTOUtils.getDTO(station)));
    }

    @Override
    public List<Train> getAllTrains() { return trainService.getAllTrains(); }

    @Override
    public void addTrain(Train train) throws AppException {
        Train added = trainService.addTrain(train);
        loggedClients.forEach((k, obs) -> obs.trainAdded(DTOUtils.getDTO(added)));
    }

    @Override
    public void updateTrain(Train train) throws AppException {
        Train updated = trainService.updateTrain(train);
        loggedClients.forEach((k, obs) -> obs.trainUpdated(DTOUtils.getDTO(updated)));
    }

    @Override
    public void removeTrain(Train train) throws AppException {
        trainService.deleteTrain(train);
        loggedClients.forEach((k, obs) -> obs.trainDeleted(DTOUtils.getDTO(train)));
    }

    @Override
    public List<Schedule> getAllSchedules() { return scheduleService.getAllSchedules(); }

    @Override
    public void addSchedule(Schedule schedule) throws AppException {
        Schedule added = scheduleService.addSchedule(schedule);
        loggedClients.forEach((k, obs) -> obs.scheduleAdded(DTOUtils.getDTO(added)));
    }

    @Override
    public void updateSchedule(Schedule schedule) throws AppException {
        Schedule before = scheduleService.findById(schedule.getId());
        int oldDelay = before == null ? 0 : before.getDelayMinutes();
        String oldStatus = before == null ? null : before.getStatus();

        Schedule updated = scheduleService.updateSchedule(schedule);

        boolean delayIncreased = updated.getDelayMinutes() > oldDelay && updated.getDelayMinutes() > 0;
        boolean nowCancelled = "CANCELLED".equalsIgnoreCase(updated.getStatus())
                && !"CANCELLED".equalsIgnoreCase(oldStatus);

        if (nowCancelled) {
            bookingService.notifyCancelledSchedule(updated);
        } else if (delayIncreased) {
            bookingService.notifyDelayedSchedule(updated);
        }

        loggedClients.forEach((k, obs) -> obs.scheduleUpdated(DTOUtils.getDTO(updated)));
    }

    @Override
    public void removeSchedule(Schedule schedule) throws AppException {
        scheduleService.deleteSchedule(schedule);
        loggedClients.forEach((k, obs) -> obs.scheduleDeleted(DTOUtils.getDTO(schedule)));
    }

    @Override
    public List<AvailableScheduleDTO> searchAvailableSchedules(JourneySearchDTO criteria) {
        return journeySearchService.searchDirect(criteria);
    }

    @Override
    public List<JourneyDTO> searchJourneys(JourneySearchDTO criteria) {
        return journeySearchService.searchJourneys(criteria);
    }

    @Override
    public BookingDTO bookSeats(BookingRequestDTO request) throws AppException {
        Booking saved = bookingService.book(request);
        BookingDTO dto = DTOUtils.getDTO(saved);
        loggedClients.forEach((k, obs) -> obs.bookingAdded(dto));
        return dto;
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @Override
    public List<BookingDTO> getBookingsForUser(String username) {
        return bookingService.getBookingsForUser(username);
    }
}
