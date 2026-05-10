package networking.handlers;

import dtos.BookingDTO;
import dtos.BookingRequestDTO;
import exceptions.AppException;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class BookSeatsHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        try {
            BookingRequestDTO req = (BookingRequestDTO) request.getData();
            BookingDTO saved = server.bookSeats(req);
            return Response.ok(saved);
        } catch (AppException e) {
            return Response.error(e.getMessage());
        } catch (RuntimeException e) {
            return Response.error("Booking failed: " + e.getMessage());
        }
    }
}
