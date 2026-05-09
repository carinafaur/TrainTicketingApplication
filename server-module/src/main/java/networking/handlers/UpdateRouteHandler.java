package networking.handlers;

import domain.Route;
import domain.Station;
import dtos.RouteDTO;
import exceptions.AppException;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class UpdateRouteHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        RouteDTO dto = (RouteDTO) request.getData();
        Station start = server.findStationById(dto.getStartStationId());
        Station end = server.findStationById(dto.getDestinationStationId());
        Route route = new Route(start, end);
        route.setId(dto.getId());
        try {
            server.updateRoute(route);
            return Response.ok(dto);
        } catch (AppException e) {
            return Response.error(e.getMessage());
        }
    }
}
