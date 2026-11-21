package org.parking.model;

import java.util.ArrayList;
import java.util.List;

public class ParkingArea {
    private final List<ParkingPlace> places = new ArrayList<>();

    public ParkingArea(int totalPlaces){
        for (int i = 1; i <= totalPlaces; i++){
            places.add(new ParkingPlace(i));
        }
    }

    public List<ParkingPlace> getPlaces() {
        return places;
    }

    public long getFreePlacesCount(){
        return places.stream().filter(ParkingPlace::isSlotFree).count();
    }

    public long getTookPlacesCount(){
        return places.size() - getFreePlacesCount();
    }
}
