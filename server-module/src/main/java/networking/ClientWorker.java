package networking;

import dtos.RouteDTO;
import dtos.TrainDTO;
import networking.handlers.HandlerRegistry;
import networking.handlers.RequestHandler;
import service.IObserver;
import service.IService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Per-connection worker. It owns the socket I/O, dispatches incoming requests
 * via the {@link HandlerRegistry}, and pushes observer notifications back to
 * the connected client. All business logic lives in the handlers and in
 * {@link IService} (MasterService).
 */
public class ClientWorker implements Runnable, IObserver {

    /**
     * Single shared registry — handlers are stateless.
     */
    private static final HandlerRegistry REGISTRY = new HandlerRegistry();

    private final IService server;
    private final Socket connection;
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
                Request request = (Request) input.readObject();
                Response response = dispatch(request);
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

    private Response dispatch(Request request) {
        RequestHandler handler = REGISTRY.get(request.getType());
        if (handler == null) {
            return Response.error("Unknown request type: " + request.getType());
        }
        return handler.handle(request, server, this);
    }

    // ---------------------------------------------- Observer push to client

    @Override
    public void routeAdded(RouteDTO newRoute) {
        sendQuiet(new Response(ResponseType.ADDED, newRoute));
    }

    @Override
    public void routeDeleted(RouteDTO oldRoute) {
        sendQuiet(new Response(ResponseType.REMOVED, oldRoute));
    }

    @Override
    public void routeUpdated(RouteDTO upd) {
        sendQuiet(new Response(ResponseType.UPDATED, upd));
    }

    @Override
    public void trainAdded(TrainDTO t) {
        sendQuiet(new Response(ResponseType.TRAIN_ADDED, t));
    }

    @Override
    public void trainDeleted(TrainDTO t) {
        sendQuiet(new Response(ResponseType.TRAIN_REMOVED, t));
    }

    @Override
    public void trainUpdated(TrainDTO t) {
        sendQuiet(new Response(ResponseType.TRAIN_UPDATED, t));
    }

    private void sendQuiet(Response res) {
        try {
            sendResponse(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
