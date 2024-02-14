package uk.tw.energy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.exceptions.InvalidElectricityReadingException;
import uk.tw.energy.exceptions.InvalidMeterIdException;

/**
 * Service class that handles all the validation logic for a meterReading object.
 */
@Service
public class MeterReadingValidationService {
	// regex for a valid meter id (regex = smart-meter-[any number])
	private final String validMeterIdRegex = "smart-meter-[0-9]+";

	/**
	 * Finds if the meter reading is valid or not. Validates meterId and electricity
	 * readings
	 * 
	 * @return true or false depending on whether the meter reading is valid or not
	 */
	public void validateMeterReading(String smartMeterId, List<ElectricityReading> electricityReadings)
				throws InvalidMeterIdException, InvalidElectricityReadingException {
		if (!isMeterIdValid(smartMeterId)) {
			throw new InvalidMeterIdException(String.format("Smart meter id is not valid %s", smartMeterId));
		}
		
		if (!areElectricityReadingsValid(electricityReadings)) {
			throw new InvalidElectricityReadingException("Electricity Readings are not valid");
		}
	}

	/**
	 * Checks if given meter id is valid or not. Assumption:- format for a valid
	 * meter id is - smart-meter-{number(digit)}
	 * 
	 * @param smartMeterId - smart meter id provided by the user
	 * @return true if meterId is valid false otherwise
	 */
	private boolean isMeterIdValid(String smartMeterId) {
		return smartMeterId != null && !smartMeterId.isEmpty() && smartMeterId.matches(validMeterIdRegex);
	}

	/**
	 * Checks if given electricity readings are valid or not Assumption - for
	 * readings that are invalid i.e. either don't have the required parameters or
	 * missing a parameter. eg. {"read":0.0503} or {"tim":1606636800,
	 * "reading":0.0503} We individually check if reading or time is null for any
	 * electricity reading.
	 * 
	 * @param electricityReadings - readings provided by the user
	 * @return true if reading is valid false otherwise
	 */
	private boolean areElectricityReadingsValid(List<ElectricityReading> electricityReadings) {
		return electricityReadings != null && !electricityReadings.isEmpty() && !electricityReadings.stream()
				.filter(reading -> reading.time() == null || reading.reading() == null).findFirst().isPresent();
	}
}
