package org.parking.ui;

import org.parking.model.Car;
import org.parking.model.ParkingArea;
import org.parking.model.ParkingPlace;
import org.parking.service.Journal;
import org.parking.service.Payment;
import org.parking.service.SimulationClock;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainView extends BorderPane {

    private final ParkingArea parkingArea = new ParkingArea(20);
    private final SimulationClock clock = new SimulationClock();
    private final Journal journal = new Journal();

    private final List<Car> availableCars = new ArrayList<>();

    private final FlowPane availableCarsPane = new FlowPane(10, 10);
    private final GridPane parkingGrid = new GridPane();
    private final HBox topPanel = new HBox();

    private final Label countersLabel = new Label();
    private final Label revenueLabel = new Label();
    private final TextArea messageArea = new TextArea();

    private final Map<ParkingPlace, StackPane> placePanes = new HashMap<>();

    private LocalDate currentDay = LocalDate.of(2027, 1, 1);

    // Ссылка на машину, которую сейчас тащат
    private Car draggedCar = null;

    public MainView() {
        clock.start();
        buildUI();
        updateCounters();
        updateRevenue();
        setupTimeListener();
    }

    private void buildUI() {
        // --- ВЕРХНЯЯ ПАНЕЛЬ ---
        Label clockLabel = new Label();
        clockLabel.textProperty().bind(clock.currentTimeProperty().asString());
        clockLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        countersLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        revenueLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        revenueLabel.setTextFill(Color.DARKGREEN);

        VBox topInfo = new VBox(5, clockLabel, countersLabel, revenueLabel);
        topInfo.setAlignment(Pos.TOP_RIGHT);
        topInfo.setPadding(new Insets(10, 20, 10, 10));

        topPanel.getChildren().add(topInfo);
        topPanel.setAlignment(Pos.CENTER_RIGHT);
        topPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        setTop(topPanel);

        // --- ЦЕНТР (Доступные машины) ---
        availableCarsPane.setPadding(new Insets(15));
        availableCarsPane.setStyle("-fx-background-color: #555555;");

        for (int i = 0; i < 12; i++) {
            availableCars.add(new Car());
        }
        refreshAvailableCarsPane();

        ScrollPane carsScroll = new ScrollPane(availableCarsPane);
        carsScroll.setFitToWidth(true);
        carsScroll.setPrefHeight(150);
        carsScroll.setStyle("-fx-background: #555555; -fx-border-color: #333333;");
        setCenter(carsScroll);

        // --- НИЗ (Парковка + Лог) ---
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
            placePanes.put(place, spotPane);

            // ОБРАБОТКА ВЪЕЗДА (DROP)
            spotPane.setOnDragOver(event -> {
                if (event.getGestureSource() != spotPane && event.getDragboard().hasString() && place.isSlotFree()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            spotPane.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString() && place.isSlotFree()) {
                    Car droppedCar = draggedCar != null ? draggedCar : findCarByPlate(db.getString());

                    if (droppedCar != null && !isCarParked(droppedCar)) {
                        // 1. Паркуем в модель и визуально
                        place.parkCar(droppedCar, clock.getCurrentTime());

                        Node parkedCarNode = createCarNode(droppedCar);
                        spotPane.getChildren().add(parkedCarNode);

                        // 2. Удаляем машину из списка доступных и добавляем новую
                        availableCars.removeIf(c -> c == droppedCar);
                        availableCarsPane.getChildren().removeIf(node ->
                                node.getUserData() instanceof Car c && c == droppedCar);

                        Car newCar = new Car();
                        availableCars.add(newCar);
                        availableCarsPane.getChildren().add(createCarNode(newCar));

                        // 3. Обновляем счетчики, журнал и лог
                        updateCounters();
                        journal.logEntry(droppedCar.getNumberPlate(), place.getSlotNumber(), clock.getCurrentTime());
                        messageArea.appendText("Въезд: " + droppedCar.getNumberPlate() + " на место №" + place.getSlotNumber() + "\n");

                        draggedCar = null;
                        success = true;
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            // ОБРАБОТКА КЛИКА
            spotPane.setOnMouseClicked(event -> {
                if (!place.isSlotFree()) {
                    Car car = place.getParkedCar();
                    LocalDateTime entry = place.getEntryTime();
                    LocalDateTime now = clock.getCurrentTime();
                    long cost = Payment.payment(entry, now);
                    Duration d = Duration.between(entry, now);

                    String info = String.format(
                            "Инфо: %s | Въезд: %s | Стоянка: %dч %dм %dс | Стоимость: %d ₽\n",
                            car.getNumberPlate(), entry.toLocalTime(),
                            d.toHours(), d.toMinutesPart(), d.toSecondsPart(), cost);
                    messageArea.appendText(info);
                }
            });

            int col = (place.getSlotNumber() - 1) % 5;
            int row = (place.getSlotNumber() - 1) / 5;
            parkingGrid.add(spotPane, col, row);
        }

        messageArea.setEditable(false);
        messageArea.setPrefHeight(120);
        messageArea.setStyle("-fx-font-family: 'monospaced';");

        VBox bottomBox = new VBox(10, parkingGrid, messageArea);
        setBottom(bottomBox);

        setupExitHandlers();
    }

    private void refreshAvailableCarsPane() {
        availableCarsPane.getChildren().clear();
        for (Car car : availableCars) {
            availableCarsPane.getChildren().add(createCarNode(car));
        }
    }

    private void registerExitTarget(Node target) {
        target.setOnDragOver(event -> {
            if (event.getDragboard().hasString() && draggedCar != null && isCarParked(draggedCar)) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });
        target.setOnDragDropped(event -> {
            boolean success = handleCarExit();
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void setupExitHandlers() {
        registerExitTarget(this);
        registerExitTarget(topPanel);
        registerExitTarget(availableCarsPane);
        registerExitTarget(parkingGrid);
        registerExitTarget(messageArea);
    }

    private boolean handleCarExit() {
        Car exitingCar = draggedCar;
        if (exitingCar == null) return false;

        ParkingPlace place = findPlaceByCar(exitingCar);
        if (place == null) return false;

        LocalDateTime exitTime = clock.getCurrentTime();
        long cost = Payment.payment(place.getEntryTime(), exitTime);

        journal.logExit(exitingCar.getNumberPlate(), place.getEntryTime(), exitTime, cost);
        messageArea.appendText("Выезд: " + exitingCar.getNumberPlate() + " | Оплачено: " + cost + " ₽\n");

        place.unPark();
        updateCounters();
        updateRevenue();

        StackPane spotPane = placePanes.get(place);
        if (spotPane != null) {
            spotPane.getChildren().removeIf(node -> node.getUserData() == exitingCar);
        }

        draggedCar = null;
        return true;
    }

    private void setupTimeListener() {
        clock.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (newTime.toLocalDate().isAfter(currentDay)) {
                LocalDateTime endOfDay = LocalDateTime.of(currentDay, LocalTime.of(23, 59, 59));
                journal.logDaySummary(endOfDay);
                journal.logNewDay(newTime);
                messageArea.appendText("--- Итог дня " + currentDay + " подведен в журнале ---\n");
                currentDay = newTime.toLocalDate();
                updateRevenue();
            }
        });
    }

    public void saveIntermediateSummary() {
        journal.logDaySummary(clock.getCurrentTime());
    }

    private void updateCounters() {
        long free = parkingArea.getFreePlacesCount();
        long took = parkingArea.getTookPlacesCount();
        countersLabel.setText("Свободно: " + free + " | Занято: " + took);
    }

    private void updateRevenue() {
        revenueLabel.setText("Выручка за день: " + journal.getAmountToday() + " ₽ | Всего: " + journal.getTotalAmount() + " ₽");
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
            draggedCar = car;
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
        if (plate == null) return null;
        for (Node node : availableCarsPane.getChildren()) {
            if (node.getUserData() instanceof Car car && car.getNumberPlate().equals(plate)) {
                return car;
            }
        }
        return null;
    }

    private ParkingPlace findPlaceByCar(Car car) {
        for (ParkingPlace place : parkingArea.getPlaces()) {
            if (!place.isSlotFree() && place.getParkedCar() == car) {
                return place;
            }
        }
        return null;
    }

    private boolean isCarParked(Car car) {
        return parkingArea.getPlaces().stream()
                .anyMatch(p -> !p.isSlotFree() && p.getParkedCar() == car);
    }
}
