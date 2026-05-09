package dtos;

import domain.Route;
import domain.Train;
import domain.User;

public class DTOUtils {

    public static UserDTO getDTO(User user) {
        return new UserDTO(user.getUsername(), user.getPassword());
    }

    public static RouteDTO getDTO(Route route) {
        return new RouteDTO(
                route.getId(),
                route.getStartStation().getStationName(),
                route.getEndStation().getStationName(),
                route.getStartStation().getId(),
                route.getEndStation().getId());
    }

    public static TrainDTO getDTO(Train train) {
        return new TrainDTO(train.getId(), train.getTrainNumber(), train.getCapacity());
    }
}
