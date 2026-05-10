package networking.handlers;

import dtos.JourneySearchDTO;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class SearchJourneysHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        try {
            JourneySearchDTO criteria = (JourneySearchDTO) request.getData();
            return Response.ok(server.searchJourneys(criteria));
        } catch (RuntimeException e) {
            return Response.error("Journey search failed: " + e.getMessage());
        }
    }
}
