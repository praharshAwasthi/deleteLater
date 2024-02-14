package uk.tw.energy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.exceptions.InvalidElectricityReadingException;
import uk.tw.energy.exceptions.InvalidMeterIdException;

/**
 * This service class provides methods for managing meter readings - storing and
 * retrieval.
 */
@Service
public class MeterReadingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MeterReadingService.class);

	private final Map<String, List<ElectricityReading>> meterAssociatedReadings;
	private final MeterReadingValidationService meterReadingValidationService;

	public MeterReadingService(Map<String, List<ElectricityReading>> meterAssociatedReadings,
			MeterReadingValidationService meterReadingValidationService) {
		this.meterAssociatedReadings = meterAssociatedReadings;
		this.meterReadingValidationService = meterReadingValidationService;
	}

	/**
	 * Gets the meter reading if present
	 * 
	 * @param smartMeterId - the meter id of the associated meter
	 * @return the reading of the corresponding meter id
	 */
	public Optional<List<ElectricityReading>> getReadings(String smartMeterId) {
		return Optional.ofNullable(meterAssociatedReadings.get(smartMeterId));
	}

	/**
	 * Validates and stores meter readings. Assumption - we only save if all
	 * electricity readings and the respective meterId is valid i.e. If one of the
	 * electricity reading is incorrect(not valid) we don't save the entire
	 * meterReadings object
	 * 
	 * @param meterReadings - input meterReadings
	 * @throws InvalidMeterReadingException - raises an exception if meter reading
	 *                                      is not valid
	 */
	public void storeReadings(MeterReadings meterReadings)
			throws InvalidMeterIdException, InvalidElectricityReadingException {
		String smartMeterId = meterReadings.smartMeterId();
		List<ElectricityReading> electricityReadings = meterReadings.electricityReadings();
		meterReadingValidationService.validateMeterReading(smartMeterId, electricityReadings);
		meterAssociatedReadings.computeIfAbsent(smartMeterId, k -> new ArrayList<>()).addAll(electricityReadings);
	}
}
