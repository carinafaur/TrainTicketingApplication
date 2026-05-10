package networking.handlers;

import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class GetAllRoutesHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        return Response.ok(server.getAllRoutes());
    }
}
