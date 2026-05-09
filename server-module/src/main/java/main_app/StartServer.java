package main_app;

import data.HibernateUtils;
import networking.ConcurrentServer;
import repository.RouteRepository;
import repository.ScheduleRepository;
import repository.StationRepository;
import repository.TrainRepository;
import repository.UserRepository;
import service.*;
import validators.RouteValidator;
import validators.ScheduleValidator;
import validators.StationValidator;
import validators.TrainValidator;

public class StartServer {
    public static void main(String[] args) {
        try {
            HibernateUtils.getSessionFactory();

            UserRepository userRepo = new UserRepository();
            UserService userService = new UserService(userRepo);

            StationRepository stationRepo = new StationRepository();
            StationValidator stationValidator = new StationValidator();
            StationService stationService = new StationService(stationRepo, stationValidator);

            RouteRepository routeRepo = new RouteRepository();
            RouteValidator routeValidator = new RouteValidator();
            RouteService routeService = new RouteService(routeRepo, routeValidator);

            TrainRepository trainRepo = new TrainRepository();
            TrainValidator trainValidator = new TrainValidator();
            TrainService trainService = new TrainService(trainRepo, trainValidator);

            ScheduleRepository scheduleRepo = new ScheduleRepository();
            ScheduleValidator scheduleValidator = new ScheduleValidator();
            ScheduleService scheduleService = new ScheduleService(scheduleRepo, scheduleValidator);

            IService service = new MasterService(
                    userService, routeService, stationService, trainService, scheduleService);

            int port = 55555;
            ConcurrentServer server = new ConcurrentServer(port, service);
            server.start();

        } catch (Exception e) {
            System.err.println("Server couldn't start: " + e.getMessage());
        } finally {
            HibernateUtils.closeSessionFactory();
        }
    }
}
