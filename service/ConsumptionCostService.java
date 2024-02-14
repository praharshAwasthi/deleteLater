package uk.tw.energy.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.exceptions.NoReadingsException;

/**
 * This service class provides functionality of calculating consumptions cost for different plans
 */
@Service
public class ConsumptionCostService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumptionCostService.class);
	
	private final List<PricePlan> pricePlans;
	private final MeterReadingService meterReadingService;
	
	public ConsumptionCostService(List<PricePlan> pricePlans, MeterReadingService meterReadingService) {
		this.pricePlans = pricePlans;
		this.meterReadingService = meterReadingService;
	}

	/**
	 * Finds cost for each plan using the input electricity readings. If readings
	 * are not present an exception is raised.
	 * 
	 * @param smartMeterId - meterId of the user.
	 * @return Map of plans and computed cost for each plan.
	 * @throws NoReadingsException exception if user has no meter readings currently
	 *                             available
	 */
	public Map<String, BigDecimal> computeConsumptionCostPerPlan(String smartMeterId) throws NoReadingsException {
		Optional<List<ElectricityReading>> electricityReadings = meterReadingService.getReadings(smartMeterId);
		if (!electricityReadings.isPresent()) {
			LOGGER.error("Cannot compute cost per plan as no readings were found for meter id "+smartMeterId);
			throw new NoReadingsException(String.format("No readings were found for meter id %s" , smartMeterId));
		}
		Map<String, BigDecimal> consumptionCostPerPlan = pricePlans.stream()
				.collect(Collectors.toMap(PricePlan::getPlanName, plan -> calculateCost(electricityReadings.get(), plan)));
		return consumptionCostPerPlan;
	}

	/**
	 * Calculate average cost for a electricity plan.
	 * Formula = Total Cost/Total Time Elapsed
	 * 
	 * @param electricityReadings - readings of the user
	 * @param pricePlan           - plan for which we have to calculate cost
	 * @return - cost for that plan
	 */
	private BigDecimal calculateCost(List<ElectricityReading> electricityReadings, PricePlan pricePlan) {
		BigDecimal totalCost = calculateTotalCost(electricityReadings, pricePlan);
		BigDecimal timeElapsed = calculateTimeElapsed(electricityReadings);

		BigDecimal averagedCost = totalCost.divide(timeElapsed, RoundingMode.HALF_UP);
		return averagedCost;
	}

	/**
	 * Calculate total cost of all the electricity readings for that price plan
	 * 
	 * @param electricityReadings - electricity readings of the user.
	 * @param pricePlan - one of the available price plan
	 * @return average of all the electricity readings
	 */
	private BigDecimal calculateTotalCost(List<ElectricityReading> electricityReadings, PricePlan pricePlan) {
		BigDecimal totalCostForPlan = electricityReadings.stream()
					.map(electricityReading ->  electricityReading.reading().multiply(pricePlan.getPrice(findLocalDateTimeForReading(electricityReading.time()))))
					.reduce(BigDecimal.ZERO, (reading, accumulator) -> reading.add(accumulator));
		return totalCostForPlan;
	}

	/**
	 * Calculates the total duration for which readings are recorded. endTime - last
	 * time reading was recorded startTime - first time reading was recorded
	 * 
	 * @param electricityReadings - electricity readings for the user.
	 * @return total time in gathering these readings
	 */
	private BigDecimal calculateTimeElapsed(List<ElectricityReading> electricityReadings) {
		ElectricityReading startTime = electricityReadings.stream().min(Comparator.comparing(ElectricityReading::time))
				.get();
		ElectricityReading endTime = electricityReadings.stream().max(Comparator.comparing(ElectricityReading::time)).get();

		return BigDecimal.valueOf(Duration.between(startTime.time(), endTime.time()).getSeconds() / 3600.0);
	}
	
	private LocalDateTime findLocalDateTimeForReading(Instant time) {
			return LocalDateTime.ofInstant(time, ZoneOffset.systemDefault());
	}

}
