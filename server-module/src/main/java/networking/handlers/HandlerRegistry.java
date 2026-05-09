package networking.handlers;

import networking.RequestType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Maps each {@link RequestType} to its {@link RequestHandler}. The registry
 * is populated once in the constructor and the resulting map is unmodifiable,
 * so it is safe to share a single instance between all client connections.
 *
 * <p>To add a new request type:
 * <ol>
 *   <li>Add the value to {@link RequestType}.</li>
 *   <li>Create a new handler class implementing {@link RequestHandler}.</li>
 *   <li>Register it in this constructor — nothing else changes.</li>
 * </ol>
 */
public class HandlerRegistry {

    private final Map<RequestType, RequestHandler> handlers;

    public HandlerRegistry() {
        Map<RequestType, RequestHandler> m = new EnumMap<>(RequestType.class);

        // Auth
        m.put(RequestType.LOGIN,            new LoginHandler());
        m.put(RequestType.LOGOUT,           new LogoutHandler());

        // Routes
        m.put(RequestType.GET_ALL_ROUTES,   new GetAllRoutesHandler());
        m.put(RequestType.GET_ALL_STATIONS, new GetAllStationsHandler());
        m.put(RequestType.ADD_ROUTE,        new AddRouteHandler());
        m.put(RequestType.UPDATE_ROUTE,     new UpdateRouteHandler());
        m.put(RequestType.REMOVE_ROUTE,     new RemoveRouteHandler());

        // Trains
        m.put(RequestType.GET_ALL_TRAINS,   new GetAllTrainsHandler());
        m.put(RequestType.ADD_TRAIN,        new AddTrainHandler());
        m.put(RequestType.UPDATE_TRAIN,     new UpdateTrainHandler());
        m.put(RequestType.REMOVE_TRAIN,     new RemoveTrainHandler());

        this.handlers = Map.copyOf(m);
    }

    /** Returns the handler for the given type, or {@code null} if unregistered. */
    public RequestHandler get(RequestType type) {
        return handlers.get(type);
    }
}
