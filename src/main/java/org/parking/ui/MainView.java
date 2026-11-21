package org.parking.ui;

import org.parking.model.Car;
import org.parking.model.ParkingArea;
import org.parking.model.ParkingPlace;
import org.parking.service.SimulationClock;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class MainView extends BorderPane {

    private final ParkingArea parkingArea = new ParkingArea(20);
    private final SimulationClock clock = new SimulationClock();
    private final FlowPane availableCarsPane = new FlowPane(10, 10);
    private final GridPane parkingGrid = new GridPane();

    // ВЫНОСИМ СЧЕТЧИК В ПОЛЕ КЛАССА, ЧТОБЫ ОБНОВЛЯТЬ ЕГО
    private final Label countersLabel = new Label();

    public MainView() {
        clock.start();
        buildUI();
        updateCounters(); // Инициализируем счетчик при старте
    }

    private void buildUI() {
        Label clockLabel = new Label();
        clockLabel.textProperty().bind(clock.currentTimeProperty().asString());
        clockLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        countersLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        VBox topInfo = new VBox(5, clockLabel, countersLabel);
        topInfo.setAlignment(Pos.TOP_RIGHT);
        topInfo.setPadding(new Insets(10, 20, 10, 10));

        HBox topPanel = new HBox(topInfo);
        topPanel.setAlignment(Pos.CENTER_RIGHT);
        topPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        setTop(topPanel);

        availableCarsPane.setPadding(new Insets(15));
        availableCarsPane.setStyle("-fx-background-color: #555555;");

        for (int i = 0; i < 12; i++) {
            Car car = new Car();
            availableCarsPane.getChildren().add(createCarNode(car));
        }

        ScrollPane carsScroll = new ScrollPane(availableCarsPane);
        carsScroll.setFitToWidth(true);
        carsScroll.setPrefHeight(150);
        carsScroll.setStyle("-fx-background: #555555; -fx-border-color: #333333;");
        setCenter(carsScroll);

        parkingGrid.setHgap(5);
        parkingGrid.setVgap(5);
        parkingGrid.setPadding(new Insets(20));
        parkingGrid.setAlignment(Pos.CENTER);
        parkingGrid.setStyle("-fx-background-color: #444444;");

        for (ParkingPlace place : parkingArea.getPlaces()) {
            Rectangle rect = new Rectangle(110, 70, Color.TRANSPARENT);
            rect.setStroke(Color.WHITE);
            rect.setStrokeWidth(3);

            Label placeLabel = new Label("№" + place.getSlotNumber());
            placeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            placeLabel.setTextFill(Color.LIGHTGRAY);
            placeLabel.setTranslateY(20);

            StackPane spotPane = new StackPane(rect, placeLabel);
            spotPane.setUserData(place);

            spotPane.setOnDragOver(event -> {
                if (event.getGestureSource() != spotPane && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            spotPane.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString() && place.isSlotFree()) {
                    String plateNumber = db.getString();
                    Car droppedCar = findCarByPlate(plateNumber);

                    if (droppedCar != null) {
                        place.parkCar(droppedCar, clock.getCurrentTime());

                        Node carNode = createCarNode(droppedCar);
                        // Отключаем перетаскивание у припаркованной машины (пока что)
                        carNode.setOnDragDetected(null);
                        spotPane.getChildren().add(carNode);

                        availableCarsPane.getChildren().removeIf(node -> node.getUserData() instanceof Car c && c.getNumberPlate().equals(plateNumber));
                        availableCarsPane.getChildren().add(createCarNode(new Car()));

                        updateCounters(); // ОБНОВЛЯЕМ СЧЕТЧИК
                        success = true;
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            int col = (place.getSlotNumber() - 1) % 5;
            int row = (place.getSlotNumber() - 1) / 5;
            parkingGrid.add(spotPane, col, row);
        }
        setBottom(parkingGrid);
    }

    // МЕТОД ДЛЯ ОБНОВЛЕНИЯ СЧЕТЧИКОВ
    private void updateCounters() {
        long free = parkingArea.getFreePlacesCount();
        long occupied = parkingArea.getTookPlacesCount();
        countersLabel.setText("Свободно: " + free + " | Занято: " + occupied);
    }

    private StackPane createCarNode(Car car) {
        Rectangle body = new Rectangle(100, 50, Color.DARKBLUE);
        body.setArcWidth(15);
        body.setArcHeight(15);
        body.setStroke(Color.BLACK);

        Rectangle windshield = new Rectangle(80, 12, Color.LIGHTBLUE);
        windshield.setArcWidth(5);
        windshield.setArcHeight(5);
        StackPane.setAlignment(windshield, Pos.TOP_CENTER);
        StackPane.setMargin(windshield, new Insets(5, 0, 0, 0));

        Text carText = new Text(car.getNumberPlate());
        carText.setFont(Font.font("System", FontWeight.BOLD, 11));
        carText.setFill(Color.WHITE);

        StackPane carNode = new StackPane(body, windshield, carText);
        carNode.setPadding(new Insets(5));
        carNode.setUserData(car);

        carNode.setOnDragDetected(event -> {
            Dragboard db = carNode.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(car.getNumberPlate());
            db.setContent(content);
            db.setDragView(body.snapshot(null, null));
            event.consume();
        });

        return carNode;
    }

    private Car findCarByPlate(String plate) {
        for (Node node : availableCarsPane.getChildren()) {
            if (node.getUserData() instanceof Car car) {
                if (car.getNumberPlate().equals(plate)) {
                    return car;
                }
            }
        }
        return null;
    }
}
