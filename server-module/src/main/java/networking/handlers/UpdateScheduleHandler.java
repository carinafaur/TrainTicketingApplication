package networking.handlers;

import domain.Schedule;
import dtos.ScheduleDTO;
import exceptions.AppException;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class UpdateScheduleHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        ScheduleDTO dto = (ScheduleDTO) request.getData();
        try {
            Schedule schedule = ScheduleDTOMapper.toEntity(dto, server);
            schedule.setId(dto.getId());
            server.updateSchedule(schedule);
            return Response.ok(dto);
        } catch (AppException e) {
            return Response.error(e.getMessage());
        } catch (RuntimeException e) {
            return Response.error("Invalid schedule data: " + e.getMessage());
        }
    }
}
