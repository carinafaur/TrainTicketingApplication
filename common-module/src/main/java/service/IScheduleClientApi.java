package service;

import dtos.ScheduleDTO;
import exceptions.AppException;

import java.util.List;

/**
 * Client-side wire API for schedules. The standard {@link IScheduleService}
 * works with entity types ({@code Schedule}), which is convenient on the
 * server but awkward on the client because the client receives DTOs over
 * the network. This interface mirrors the schedule operations using DTOs
 * directly, so the client UI can work without ever materialising
 * {@code Schedule} entities.
 *
 * <p>Implemented by {@code ServerProxy} on the client side. The server
 * does not implement it — handlers convert DTOs into entities and call
 * {@link IScheduleService} on {@code MasterService} instead.
 */
public interface IScheduleClientApi {

    void addScheduleDTO(ScheduleDTO dto) throws AppException;
    void updateScheduleDTO(ScheduleDTO dto) throws AppException;
    void removeScheduleDTO(ScheduleDTO dto) throws AppException;
    List<ScheduleDTO> getAllScheduleDTOs();
}
