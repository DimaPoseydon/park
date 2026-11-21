package org.parking.service;

import java.time.Duration;
import java.time.LocalDateTime;

public class Payment {
    public static long payment(LocalDateTime entryTime, LocalDateTime exitTime){
        Duration duration = Duration.between(entryTime, exitTime);

        long totalMinutes = duration.toMinutes();
        long totalSeconds = duration.toSeconds();

        if (totalSeconds <= 20 * 60){
            return 0;
        }

        if (totalMinutes <= 12 * 60){
            long hours = duration.toHours();

            if (duration.toMinutesPart() > 0 || duration.toSecondsPart() > 0){
                hours++;
            }
            return hours * 200;
        }

        long days = duration.toDays();

        if (duration.toHoursPart() > 0 || duration.toMinutesPart() > 0 || duration.toSecondsPart() > 0) {
            days++;
        }
        return days * 200;
    }
}
