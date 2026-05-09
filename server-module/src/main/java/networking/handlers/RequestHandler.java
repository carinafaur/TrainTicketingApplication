package networking.handlers;

import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

/**
 * One handler per {@link networking.RequestType}. Stateless, so a single
 * instance is shared between all client connections.
 */
public interface RequestHandler {
    Response handle(Request request, IService server, IObserver observer);
}
