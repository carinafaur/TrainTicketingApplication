package networking.handlers;

import domain.Schedule;
import dtos.DTOUtils;
import dtos.ScheduleDTO;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

import java.util.ArrayList;
import java.util.List;

public class GetAllSchedulesHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        List<Schedule> all = server.getAllSchedules();
        List<ScheduleDTO> dtos = new ArrayList<>(all.size());
        for (Schedule s : all) dtos.add(DTOUtils.getDTO(s));
        return Response.ok(dtos);
    }
}
