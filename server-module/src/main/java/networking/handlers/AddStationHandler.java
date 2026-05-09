package networking.handlers;

import domain.Station;
import dtos.StationDTO;
import exceptions.AppException;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class AddStationHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        StationDTO dto = (StationDTO) request.getData();
        Station station = new Station(dto.getStationName(), dto.getStationCity());
        try {
            server.addStation(station);
            return Response.ok(dto);
        } catch (AppException e) {
            return Response.error(e.getMessage());
        }
    }
}
