package org.parking.model;

import java.time.LocalDateTime;

public class ParkingPlace {
    private final int slotNumber;
    private LocalDateTime entryTime;
    private Car parkedCar;

    public ParkingPlace(int slotNumber){
        this.slotNumber = slotNumber;
    }

    public boolean isSlotFree(){
        return parkedCar == null;
    }

    public void parkCar(Car car, LocalDateTime time){
        this.parkedCar = car;
        this.entryTime = time;
    }

    public Car unPark(){
        Car carToReturn = this.parkedCar;
        this.parkedCar = null;
        this.entryTime = null;

        return carToReturn;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public Car getParkedCar() {
        return parkedCar;
    }
}
