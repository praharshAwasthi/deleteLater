package uk.tw.energy.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.exceptions.InvalidElectricityReadingException;
import uk.tw.energy.exceptions.InvalidMeterIdException;
import uk.tw.energy.service.MeterReadingService;

/**
 * Rest controller for storing and retrieving meter readings.
 */
@RestController
@RequestMapping("/readings")
public class MeterReadingController {
	
    private final MeterReadingService meterReadingService;

    public MeterReadingController(MeterReadingService meterReadingService) {
        this.meterReadingService = meterReadingService;
    }

    /**
     * Stores meter readings for the given smart meter id.
     * 
     * @param meterReadings - MeterReadings object with readings and id values.
     * @return HttpStatus 200 with acknowledgement message
     */
    @PostMapping("/store")
    public ResponseEntity<String> storeReadings(@RequestBody MeterReadings meterReadings) {
    		meterReadingService.storeReadings(meterReadings);
    		return ResponseEntity.ok("Readings Saved");  
    }

    /**
     * Retrieves readings for a given meter id if present.
     * Else NotFound (Http 404)
     * 
     * @param smartMeterId - meter id for which readings need to be retrieved
     * @return readings or Http 404 if no readings found.
     */
    @GetMapping("/read/{smartMeterId}")
    public ResponseEntity<List<ElectricityReading>> readReadings(@PathVariable String smartMeterId) {
        Optional<List<ElectricityReading>> readings = meterReadingService.getReadings(smartMeterId);
        return readings.isPresent()
                ? ResponseEntity.ok(readings.get())
                : ResponseEntity.notFound().build();
    }
    
    /**
     * Exception handler when meter reading provided is not valid. 
     * @param ex InvalidMeterReadingException object
     * @return Http bad request 400
     */
  	@ExceptionHandler({InvalidMeterIdException.class, InvalidElectricityReadingException.class})
  	@ResponseStatus(HttpStatus.BAD_REQUEST)
  	public ResponseEntity<String> handleInvalidMeterReadingException(RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  	}
  	
  	/**
  	 * Handles Exception when the params cannot be parsed.
  	 * Example electricityReading: {"time" : "HelloWorld", "reading": "Coding is fun"}
  	 * @param ex HttpMessageConversionException object 
  	 * @return Http 422 Unprocessable Entity
  	 */
  	@ExceptionHandler(HttpMessageNotReadableException.class)
  	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  	public ResponseEntity<String> handleJsonParseException(HttpMessageConversionException ex) {
  		ex.printStackTrace();
  		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Failed to parse incoming data");
  	}
}
