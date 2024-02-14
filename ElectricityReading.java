package uk.tw.energy.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Model class for Electricity Reading. 
 * Has two properties time (Instant) and reading (BigDecimal)
 */
public record ElectricityReading(Instant time, BigDecimal reading) {

}
