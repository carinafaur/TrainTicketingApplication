package networking.handlers;

import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

/**
 * One handler per {@link networking.RequestType}. Stateless, so a single
 * instance is shared between all client connections.
 *
 * @param request   the wire request, with type + payload DTO
 * @param server    the business-logic facade (MasterService)
 * @param observer  the calling client's observer (the ClientWorker itself);
 *                  needed for handlers like LOGIN that register the client
 *                  for push notifications
 * @return a non-null {@link Response} that ClientWorker will write back
 */
public interface RequestHandler {
    Response handle(Request request, IService server, IObserver observer);
}
