package org.parking.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Car{

    private final String numberPlate;

    private static final List<String> ALLOWED_LETTERS = new ArrayList<>();
    private static final List<Byte> NUMBERS = new ArrayList<>();
    private static final List<Integer> REGIONS = new ArrayList<>();

    private static final Random random = new Random();

    private static final int[] regionCodes = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
            21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
            61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
            82, 83, 86, 87, 89, 90, 91, 92, 93, 95, 96, 97, 98, 99,
            102, 103, 116, 123, 124, 125, 126, 136, 138, 142, 147, 150, 152, 154, 155, 156, 159, 161, 163, 164,
            170, 172, 173, 174, 175, 177, 178, 186, 189, 190, 193, 195, 196, 197, 198, 199,
            750, 761, 763, 764, 777, 778, 790, 798, 799, 877, 878
    };

    static {
        ALLOWED_LETTERS.add("А");
        ALLOWED_LETTERS.add("В");
        ALLOWED_LETTERS.add("Е");
        ALLOWED_LETTERS.add("К");
        ALLOWED_LETTERS.add("М");
        ALLOWED_LETTERS.add("Н");
        ALLOWED_LETTERS.add("О");
        ALLOWED_LETTERS.add("Р");
        ALLOWED_LETTERS.add("С");
        ALLOWED_LETTERS.add("Т");
        ALLOWED_LETTERS.add("У");
        ALLOWED_LETTERS.add("Х");

        for (byte i = 0; i <= 9; i++){
            NUMBERS.add(i);
        }

        for (int region : regionCodes){
            REGIONS.add(region);
        }
    }

    public Car(){
        this.numberPlate = generateNewNumber();
    }

    private String generateNewNumber(){
        String letter1 = ALLOWED_LETTERS.get(random.nextInt(ALLOWED_LETTERS.size()));
        String letter2 = ALLOWED_LETTERS.get(random.nextInt(ALLOWED_LETTERS.size()));
        String letter3 = ALLOWED_LETTERS.get(random.nextInt(ALLOWED_LETTERS.size()));

        String num1 = String.valueOf(NUMBERS.get(random.nextInt(NUMBERS.size())));
        String num2 = String.valueOf(NUMBERS.get(random.nextInt(NUMBERS.size())));
        String num3 = String.valueOf(NUMBERS.get(random.nextInt(NUMBERS.size())));
        String numbers = num1 + num2 + num3;

        int region = REGIONS.get(random.nextInt(REGIONS.size()));

        return String.format("%s %s %s%s %d", letter1, numbers, letter2, letter3, region);
    }

    public String getNumberPlate(){
        return numberPlate;
    }

    @Override
    public String toString(){
        return numberPlate;
    }
}
