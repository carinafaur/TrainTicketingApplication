package networking.handlers;

import domain.Station;
import dtos.StationDTO;
import exceptions.AppException;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class UpdateStationHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        StationDTO dto = (StationDTO) request.getData();
        Station station = new Station(dto.getStationName(), dto.getStationCity());
        station.setId(dto.getId());
        try {
            server.updateStation(station);
            return Response.ok(dto);
        } catch (AppException e) {
            return Response.error(e.getMessage());
        }
    }
}
