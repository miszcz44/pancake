package com.example.eval2;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/pancake")
@AllArgsConstructor
public class HomeController {
    @PostMapping
    public List<Object> aaa(@RequestBody List<Pancake> pancakes) {
        DateTimeFormatter zonedFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ssXXX");
        ZoneId demandedZone = ZoneId.of("Europe/Warsaw");
        pancakes.stream().forEach(pancake -> {
                ZonedDateTime originalTimestamp = ZonedDateTime.from(zonedFormatter.parse(pancake.getTimestamp()));
                ZonedDateTime demandedTimestamp = originalTimestamp.toLocalDateTime()
                        .atZone(originalTimestamp.getZone())
                        .withZoneSameInstant(demandedZone)
                        .truncatedTo(ChronoUnit.HOURS);
                pancake.setTimestamp(zonedFormatter.format(demandedTimestamp));
        });
        Map<String, double[]> sumByTimestamp = new HashMap<>();
        for(Pancake pancake : pancakes) {
            if(!sumByTimestamp.containsKey(pancake.getTimestamp())) {
                sumByTimestamp.put(pancake.getTimestamp(), new double[]{0.0, 0.0, 0.0, 0.0});
            }
            double[] sums = sumByTimestamp.get(pancake.getTimestamp());
            sums[0] += pancake.getFlour()/100.0;
            sums[1] += pancake.getGroat()/1000.0;
            sums[2] += pancake.getMilk()/1000.0;
            sums[3] += pancake.getEgg();
        }
        List<Object> sumList = new ArrayList<>();
        for(String timestamp : sumByTimestamp.keySet()) {
            double[] elements = sumByTimestamp.get(timestamp);
            String[] strElements = new String[4];
            DecimalFormat df = new DecimalFormat("#.##");
            for(int i=0; i<elements.length; i++) {
                strElements[i] = df.format(elements[i]).replace(",", ".");
                if(strElements[i].endsWith(".0")) {
                    strElements[i] = strElements[i].replace(".0", "");
                }
            }
            Map<String, String> jsonKeysAndValues = new HashMap<>();
            jsonKeysAndValues.put("TIMESTAMP", timestamp);
            jsonKeysAndValues.put("EGG", strElements[3]);
            jsonKeysAndValues.put("MILK", strElements[2]);
            jsonKeysAndValues.put("GROAT", strElements[1]);
            jsonKeysAndValues.put("FLOUR", strElements[0]);
            sumList.add(jsonKeysAndValues);
        }
        return sumList;
    }
    @PostMapping("/contract")
    public List<Contract> bbb(@RequestBody List<Contract> contracts) {
        DateTimeFormatter zonedFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        List<Contract> unemployedIntervals = new ArrayList<>();
        contracts.sort(Comparator.comparing(Contract::getStart));
        ZoneId demandedZone = ZoneId.of("Europe/Warsaw");
        ZonedDateTime intervalStart = ZonedDateTime.parse("2022-10-25T00:00:00+02:00");
        ZonedDateTime intervalEnd = ZonedDateTime.parse("2023-08-11T00:00:00+02:00");
        ZonedDateTime currentStart = intervalStart;
        ZonedDateTime currentEnd = intervalStart;
        for(Contract contract : contracts) {
            ZonedDateTime startTime = contract.getStart()
                    .toLocalDateTime().atZone(contract.getStart().getZone()).withZoneSameInstant(demandedZone);
            if(startTime.isAfter(intervalEnd) || startTime.equals(intervalEnd)) {
                break;
            }
            ZonedDateTime endTime = intervalEnd;
            if(!contract.getEnd().equals("-")) {
                endTime = ZonedDateTime.parse(contract.getEnd());
                endTime = endTime.toLocalDateTime().atZone(endTime.getZone()).withZoneSameInstant(demandedZone);
            }
            if(startTime.isBefore(currentStart) || startTime.equals(currentStart)) {
                if(endTime.isAfter(currentStart) && endTime.isAfter(currentEnd)) {
                    currentEnd = endTime;
                }
            }
            else if(startTime.isAfter(currentEnd)) {
                unemployedIntervals.add(new Contract(currentEnd, zonedFormatter.format(startTime).toString()));
                currentStart = startTime;
                currentEnd = endTime;
            }
            else if((startTime.isAfter(currentStart) && startTime.isBefore(currentEnd)) || startTime.equals(currentEnd)) {
                currentStart = startTime;
                if(endTime.isAfter(currentEnd)) {
                    currentEnd = endTime;
                }
            }
        }
        List<Contract> demandedData = new ArrayList<>();
        for(Contract interval : unemployedIntervals) {
            if(interval.getStart().isAfter(intervalStart)) {
                demandedData.add(new Contract(intervalStart, zonedFormatter.format(interval.getStart()).toString()));
            }
            demandedData.add(interval);
            intervalStart = ZonedDateTime.parse(interval.getEnd());
        }
        if(!contracts.get(contracts.size()-1).getEnd().equals("-") &&
                ZonedDateTime.parse(contracts.get(contracts.size()-1).getEnd()).isBefore(intervalEnd)) {
            demandedData.get(demandedData.size()-1).setEnd(zonedFormatter.format(intervalEnd).toString());
        }
        else {
            demandedData.add(new Contract(intervalStart, zonedFormatter.format(intervalEnd).toString()));
        }
        return demandedData;
    }
}