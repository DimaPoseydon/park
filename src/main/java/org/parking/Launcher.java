package org.parking;

import org.parking.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainView view = new MainView();
        Scene scene = new Scene(view, 800, 700);

        primaryStage.setTitle("Парковка аэропорта");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
