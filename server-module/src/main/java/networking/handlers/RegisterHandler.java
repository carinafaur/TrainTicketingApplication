package networking.handlers;

import domain.User;
import dtos.RegisterRequestDTO;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class RegisterHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        RegisterRequestDTO dto = (RegisterRequestDTO) request.getData();
        try {
            User user = server.registerUser(dto, observer);
            return Response.ok(user);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }
}
