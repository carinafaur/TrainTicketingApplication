package networking.handlers;

import networking.RequestType;

import java.util.EnumMap;
import java.util.Map;

public class HandlerRegistry {

    private final Map<RequestType, RequestHandler> handlers;

    public HandlerRegistry() {
        Map<RequestType, RequestHandler> m = new EnumMap<>(RequestType.class);

        m.put(RequestType.LOGIN,             new LoginHandler());
        m.put(RequestType.LOGOUT,            new LogoutHandler());

        m.put(RequestType.GET_ALL_ROUTES,    new GetAllRoutesHandler());
        m.put(RequestType.ADD_ROUTE,         new AddRouteHandler());
        m.put(RequestType.UPDATE_ROUTE,      new UpdateRouteHandler());
        m.put(RequestType.REMOVE_ROUTE,      new RemoveRouteHandler());

        m.put(RequestType.GET_ALL_STATIONS,  new GetAllStationsHandler());
        m.put(RequestType.ADD_STATION,       new AddStationHandler());
        m.put(RequestType.UPDATE_STATION,    new UpdateStationHandler());
        m.put(RequestType.REMOVE_STATION,    new RemoveStationHandler());

        m.put(RequestType.GET_ALL_TRAINS,    new GetAllTrainsHandler());
        m.put(RequestType.ADD_TRAIN,         new AddTrainHandler());
        m.put(RequestType.UPDATE_TRAIN,      new UpdateTrainHandler());
        m.put(RequestType.REMOVE_TRAIN,      new RemoveTrainHandler());

        m.put(RequestType.GET_ALL_SCHEDULES, new GetAllSchedulesHandler());
        m.put(RequestType.ADD_SCHEDULE,      new AddScheduleHandler());
        m.put(RequestType.UPDATE_SCHEDULE,   new UpdateScheduleHandler());
        m.put(RequestType.REMOVE_SCHEDULE,   new RemoveScheduleHandler());

        m.put(RequestType.SEARCH_AVAILABLE,  new SearchAvailableHandler());
        m.put(RequestType.BOOK_SEATS,        new BookSeatsHandler());
        m.put(RequestType.GET_ALL_BOOKINGS,  new GetAllBookingsHandler());
        m.put(RequestType.GET_MY_BOOKINGS,   new GetMyBookingsHandler());

        this.handlers = Map.copyOf(m);
    }

    public RequestHandler get(RequestType type) {
        return handlers.get(type);
    }
}
