package networking.handlers;

import dtos.UserDTO;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class LogoutHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        UserDTO dto = (UserDTO) request.getData();
        try {
            server.logoutUser(dto.getUsername(), observer);
            return Response.ok(null);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }
}
