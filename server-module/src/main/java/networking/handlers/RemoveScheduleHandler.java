package networking.handlers;

import domain.Schedule;
import dtos.ScheduleDTO;
import exceptions.AppException;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class RemoveScheduleHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        ScheduleDTO dto = (ScheduleDTO) request.getData();
        try {
            Schedule s = new Schedule();
            s.setId(dto.getId());
            server.removeSchedule(s);
            return Response.ok(dto);
        } catch (AppException e) {
            return Response.error(e.getMessage());
        }
    }
}
