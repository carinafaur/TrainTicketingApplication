package networking;

import domain.Route;
import domain.Station;
import domain.User;
import dtos.DTOUtils;
import dtos.RouteDTO;
import dtos.UserDTO;
import exceptions.AppException;
import exceptions.ValidationException;
import service.IObserver;
import service.IService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientWorker implements Runnable, IObserver {
    private final IService server;
    private Socket connection;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private volatile boolean connected;

    public ClientWorker(IService server, Socket connection) {
        this.server = server;
        this.connection = connection;
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (connected) {
            try {
                Object request = input.readObject();
                Response response = handleRequest((Request) request);
                if (response != null) {
                    sendResponse(response);
                }
            } catch (IOException | ClassNotFoundException e) {
                connected = false;
                System.out.println("Client disconnected.");
            }
        }
        closeConnection();
    }

    private Response handleRequest(Request request) {
        if (request.getType() == RequestType.LOGIN) {
            return handleLoginRequest(request);
        } else if (request.getType() == RequestType.LOGOUT) {
            return handleLogoutRequest(request);
        } else if (request.getType() == RequestType.GET_ALL_ROUTES) {
            return handleGetAllRoutes(request);
        } else if (request.getType() == RequestType.GET_ALL_STATIONS) {
            return handleGetAllStations(request);
        } else if (request.getType() == RequestType.ADD_ROUTE) {
            return handleAddRoute(request);
        } else if (request.getType() == RequestType.UPDATE_ROUTE) {
            return handleUpdateRoute(request);
        }else if(request.getType()==RequestType.REMOVE_ROUTE){
            return handleRemoveRoute(request);
        }
        return null;
    }

    private Response handleLogoutRequest(Request request) {
        UserDTO dto = (UserDTO) request.getData();
        try {
            server.logoutUser(dto.getUsername(), this);
            return Response.ok(null);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    private Response handleLoginRequest(Request request) {
        UserDTO dto = (UserDTO) request.getData();
        try {
            User user = server.loginUser(dto.getUsername(), dto.getPassword(), this);
            return Response.ok(user);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    private Response handleGetAllStations(Request request) {
        return Response.ok(server.getAllStations());
    }

    @Override
    public void routeAdded(RouteDTO newRoute) {
        Response res = new Response(ResponseType.ADDED, newRoute);

        try {
            sendResponse(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void routeDeleted(RouteDTO oldRoute) {
        Response res = new Response(ResponseType.REMOVED, oldRoute);

        try {
            sendResponse(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void routeUpdated(RouteDTO updatedRoute) {
        Response res = new Response(ResponseType.UPDATED, updatedRoute);

        try {
            sendResponse(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response handleAddRoute(Request request) {
        RouteDTO dto = (RouteDTO) request.getData();

        Station start = server.findStationById(dto.getStartStationId());
        Station end = server.findStationById(dto.getDestinationStationId());

        Route route = new Route(start, end);

        try {
            server.addRoute(route);
            return new Response(ResponseType.OK, dto);
        } catch (AppException e) {
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    private Response handleUpdateRoute(Request request) {
        RouteDTO dto = (RouteDTO) request.getData();
        Station start = server.findStationById(dto.getStartStationId());
        Station end = server.findStationById(dto.getDestinationStationId());
        Route route = new Route(start, end);
        route.setId(dto.getId());
        try {
            server.updateRoute(route);
            return new Response(ResponseType.OK, dto);
        } catch (AppException e) {
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    private Response handleRemoveRoute(Request request) {
        RouteDTO dto = (RouteDTO) request.getData();
        Station start = server.findStationById(dto.getStartStationId());
        Station end = server.findStationById(dto.getDestinationStationId());
        Route route = new Route(start, end);
        route.setId(dto.getId());
        try{
            server.removeRoute(route);
            return new Response(ResponseType.OK, dto);
        } catch (AppException e) {
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    private Response handleGetAllRoutes(Request request) {
        return Response.ok(server.getAllRoutes());
    }

    private void sendResponse(Response response) {
        try {
            output.writeObject(response);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error sending response: " + e.getMessage());
            connected = false;
        }
    }

    private void closeConnection() {
        try {
            input.close();
            output.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
