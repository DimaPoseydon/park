package org.parking.service;

import java.time.Duration;
import java.time.LocalDateTime;

public class Payment {
    public static long payment(LocalDateTime entryTime, LocalDateTime exitTime){
        long seconds = Duration.between(entryTime, exitTime).toSeconds();

        if (seconds <= 20 * 60) {
            return 0;
        }

        if (seconds <= 12 * 60 * 60) {
            long hours = seconds / 3600;
            if (seconds % 3600 > 0) {
                hours++;
            }
            return hours * 200;
        }

        long days = seconds / (24 * 60 * 60);
        if (seconds % (24 * 60 * 60) > 0) {
            days++;
        }
        return days * 200;
    }
}
