package uk.tw.energy.domain;

import java.util.List;

/**
 * Model for MeterReading. 
 * Has two properties smartMeterId (String) and List<ElectricityReading> 
 */
public record MeterReadings(String smartMeterId, List<ElectricityReading> electricityReadings) {

}
