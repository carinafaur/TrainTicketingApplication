package service;

/**
 * Umbrella facade composed of all per-domain service contracts. Server-side
 * {@code MasterService} and client-side {@code ServerProxy} both implement
 * this single interface. Callers that only need a subset can declare a
 * narrower type — for example, a customer controller can hold an
 * {@link IBookingService} reference instead of the whole {@code IService}.
 */
public interface IService extends
        IAuthService,
        IRouteService,
        IStationService,
        ITrainService,
        IScheduleService,
        IBookingService {
}
