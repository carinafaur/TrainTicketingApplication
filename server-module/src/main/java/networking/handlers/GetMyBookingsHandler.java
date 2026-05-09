package networking.handlers;

import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class GetMyBookingsHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        String username = (String) request.getData();
        return Response.ok(server.getBookingsForUser(username));
    }
}
