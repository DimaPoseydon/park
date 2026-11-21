package org.parking.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Journal {
    private final Path logFile;
    private final Gson gson;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private int entries = 0;
    private int exites = 0;
    private long amountToday = 0;
    private long totalAmount = 0;

    public Journal(){
        String fileName = "parking_log_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";

        this.logFile = Path.of(fileName);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void logEntry(String carNumber, int placeNumber, LocalDateTime time){
        entries++;

        Map<String, Object> event = new HashMap<>();
        event.put("event", "ENTRY");
        event.put("time", time);
        event.put("carNumber", carNumber);
        event.put("placeNumber", placeNumber);

        writeJsonToFile(event);
    }

    public void logExit(String carNumber, LocalDateTime entryTime, LocalDateTime exitTime, long payment){
        exites++;
        amountToday += payment;
        totalAmount += payment;

        Map<String, Object> event = new HashMap<>();
        event.put("event", "EXIT");
        event.put("time", exitTime.format(DTF));
        event.put("carNumber", carNumber);
        event.put("entryTime", entryTime.format(DTF));
        event.put("payment", payment + " RUB");

        writeJsonToFile(event);
    }
    public void logDaySummary(LocalDateTime endOfDay){
        Map<String, Object> summary = new HashMap<>();

        summary.put("event", "DAY_SUMMARY");
        summary.put("time", endOfDay.format(DTF));
        summary.put("entries", entries);
        summary.put("exits", exites);
        summary.put("revenue", amountToday + " RUB");

        writeJsonToFile(summary);

        entries = 0;
        exites = 0;
        amountToday = 0;
    }

    private void writeJsonToFile(Map<String, Object> data) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                logFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            String jsonLine = gson.toJson(data).replace("\n", " ");
            writer.write(jsonLine);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Ошибка записи в журнал: " + e.getMessage());
        }
    }



}
