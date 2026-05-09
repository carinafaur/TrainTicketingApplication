package networking.handlers;

import domain.User;
import dtos.UserDTO;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class LoginHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        UserDTO dto = (UserDTO) request.getData();
        try {
            User user = server.loginUser(dto.getUsername(), dto.getPassword(), observer);
            return Response.ok(user);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }
}
