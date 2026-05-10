package networking.handlers;

import domain.Route;
import domain.Schedule;
import domain.ScheduleStop;
import domain.Station;
import domain.Train;
import dtos.ScheduleDTO;
import dtos.ScheduleStopDTO;
import service.IService;

final class ScheduleDTOMapper {

    private ScheduleDTOMapper() {}

    static Schedule toEntity(ScheduleDTO dto, IService server) {
        Schedule s = new Schedule();
        s.setDelayMinutes(dto.getDelayMinutes());
        s.setStatus(dto.getStatus());
        s.setDepartureTime(dto.getDepartureTime());
        s.setArrivalTime(dto.getArrivalTime());

        for (Train t : server.getAllTrains()) {
            if (t.getId() == dto.getTrainId()) { s.setTrain(t); break; }
        }
        for (Route r : server.getAllRoutes()) {
            if (r.getId() == dto.getRouteId()) { s.setRoute(r); break; }
        }

        if (dto.getStops() != null) {
            for (ScheduleStopDTO st : dto.getStops()) {
                Station station = server.findStationById(st.getStationId());
                ScheduleStop stop = new ScheduleStop(
                        station,
                        st.getStopOrder(),
                        st.getArrivalTime(),
                        st.getDepartureTime()
                );
                s.addStop(stop);
            }
        }

        return s;
    }
}
