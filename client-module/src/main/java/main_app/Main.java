package main_app;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import gui.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import networking.ServerProxy;
import service.IService;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        IService server = new ServerProxy("localhost", 55555);

        LoginController.show(server);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
