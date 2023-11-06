package com.example.eval2;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class Contract {
    private ZonedDateTime start;
    private String end;

    public Contract(ZonedDateTime start, String end) {
        this.start = start;
        this.end = end;
    }
}
