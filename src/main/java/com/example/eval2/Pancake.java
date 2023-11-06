package com.example.eval2;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Pancake {
    private String timestamp;
    private double flour;
    private double groat;
    private double milk;
    private double egg;
    public Pancake(String timestamp, double flour, double groat, double milk, double egg) {
        this.timestamp = timestamp;
        this.flour = flour;
        this.groat = groat;
        this.milk = milk;
        this.egg = egg;
    }

    public Pancake() {

    }

}
