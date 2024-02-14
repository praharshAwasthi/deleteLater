package uk.tw.energy.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.common.lang.Nullable;
import uk.tw.energy.exceptions.NoReadingsException;
import uk.tw.energy.exceptions.RecommendationLimitExccededException;
import uk.tw.energy.service.PricePlanService;

/**
 * Rest controller for comparing and recommending different plans.
 */
@RestController
@RequestMapping("/price-plans")
public class PricePlanComparatorController {
	private final PricePlanService pricePlanService;

	public PricePlanComparatorController(PricePlanService pricePlanService) {
		this.pricePlanService = pricePlanService;
	}

	/**
	 * Calculates and returns consumptions for different plans.
	 * 
	 * @param smartMeterId - meter id whose electricity readings will be used for	computation.
	 * @return cost for each plan
	 */
	@GetMapping("/compare-all/{smartMeterId}")
	public ResponseEntity<Map<String, Object>> calculateCostForEachPricePlan(@PathVariable String smartMeterId) {
		Map<String, Object> pricePlanComparisons = pricePlanService.findConsumptionCostPerPlan(smartMeterId);
		return ResponseEntity.ok(pricePlanComparisons);
	}

	/**
	 * Finds recommended plans for given smart meter id.
	 * 
	 * @param smartMeterId - smart meter id for which we want to recommend plans
	 * @param limit - the number of plans to recommend
	 * @return - List of recommended plans
	 */
	@GetMapping("/recommend/{smartMeterId}")
	public ResponseEntity<List<Map.Entry<String, BigDecimal>>> recommendCheapestPricePlans(
			@PathVariable String smartMeterId, @RequestParam(value = "limit", required = false) Integer limit) {
		List<Map.Entry<String, BigDecimal>> recommendations = 
				pricePlanService.findMeterRecommendationForUser(smartMeterId, limit);
		return ResponseEntity.ok(recommendations);
	}

	/**
	 * Exception handler when we try to recommend or calculate cost for a meter id
	 * whose readings we don't have.
	 * 
	 * @param ex - NoReadingException object
	 * @return HttpStatus Not Found is returned
	 */
	@ExceptionHandler(NoReadingsException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<String> handleResourceNotFoundException(NoReadingsException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	/**
	 * Exception handler when limit exceeds the number of recommendations we can
	 * provide.
	 * 
	 * @param ex - RecommendationLimitExccededException object
	 * @return HttpStatus Bad Request is returned
	 */
	@ExceptionHandler(RecommendationLimitExccededException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<String> handleRecommendationLimitExceededException(RecommendationLimitExccededException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	}
}
