package networking;


import service.IService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentServer {
    private int port;
    private IService serverLogic;
    private ExecutorService executor;
    private static final int THREAD_COUNT = 20;


    public ConcurrentServer(int port, IService serverLogic) {
        this.port = port;
        this.serverLogic = serverLogic;
        this.executor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected!");

                    ClientWorker worker = new ClientWorker(serverLogic, clientSocket);
                    executor.execute(worker);
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Fatal error: Could not bind to port " + port);
        } finally {
            stop();
        }
    }

    public void stop() {
        System.out.println("Stopping server and thread pool...");
        executor.shutdown();
    }
}
