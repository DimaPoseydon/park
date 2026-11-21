package org.parking;

import org.parking.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    private MainView view;

    @Override
    public void start(Stage primaryStage) {
        view = new MainView();
        Scene scene = new Scene(view, 1000, 800);

        primaryStage.setTitle("Парковка аэропорта");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (view != null) {
            view.saveIntermediateSummary();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}