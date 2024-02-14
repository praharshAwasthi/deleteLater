package uk.tw.energy.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import uk.tw.energy.exceptions.NoReadingsException;
import uk.tw.energy.exceptions.RecommendationLimitExccededException;

/**
 * This service class provides methods for price comparison and recommendation.
 */
@Service
public class PricePlanService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PricePlanService.class);

	public final static String PRICE_PLAN_ID_KEY = "pricePlanId";
	public final static String PRICE_PLAN_COMPARISONS_KEY = "pricePlanComparisons";

	private final AccountService accountService;
	private final ConsumptionCostService consumptionCostService;

	public PricePlanService(AccountService accountService, ConsumptionCostService consumptionCostService) {
		this.accountService = accountService;
		this.consumptionCostService = consumptionCostService;
	}

	/**
	 * Computes consumption of the user for different price plans. Assumption - if
	 * user has not inputed any readings we cannot compute different plan costs. In
	 * such a case raise NoReadingsException.
	 * 
	 * @param smartMeterId - meterId of the user
	 * @return Map of consumption cost for different price plans and current cost.
	 * @throws NoReadingsException exception if user has no meter readings currently
	 *                             available
	 */
	public Map<String, Object> findConsumptionCostPerPlan(String smartMeterId) throws NoReadingsException {
		Map<String, Object> pricePlanComparisons = new HashMap<>();
		String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
		Map<String, BigDecimal> consumptionCosts = consumptionCostService.computeConsumptionCostPerPlan(smartMeterId);

		pricePlanComparisons.put(PRICE_PLAN_ID_KEY, pricePlanId);
		pricePlanComparisons.put(PRICE_PLAN_COMPARISONS_KEY, consumptionCosts);
		return pricePlanComparisons;
	}

	/**
	 * Finds meter recommendation for the user based on lowest consumption cost.
	 * Assumptions - 1: we can only calculate any recommendations if user has
	 * inputed meter readings as if we dont have any readings we cannot compute 
	 * costs for different plans.
	 * If no readings found raise an exception. 
	 * 2: If limit>recommendations.size() raise an exception as we cannot provide these many recommendations.
	 * 
	 * @param smartMeterId - meterId of the user.
	 * @param limit        - number of recommendations to be returned.
	 * @return recommendation for the user
	 * @throws NoReadingsException exception if user has no meter readings currently
	 *                             available
	 */
	public List<Map.Entry<String, BigDecimal>> findMeterRecommendationForUser(String smartMeterId, Integer limit)
			throws NoReadingsException, RecommendationLimitExccededException {
		List<Map.Entry<String, BigDecimal>> recommendations = new ArrayList<>();
		Map<String, BigDecimal> pricePlanComparisons = consumptionCostService.computeConsumptionCostPerPlan(smartMeterId);
		recommendations.addAll(pricePlanComparisons.entrySet());
		recommendations.sort(Comparator.comparing(Map.Entry::getValue));

		if (limit != null && limit > recommendations.size()) {
			LOGGER.error("Number of recommendations required exceeded the possible capacity");
			throw new RecommendationLimitExccededException(
					String.format("Cannot display more than %d plan recommendations", recommendations.size()));
		}

		if (limit != null && limit < recommendations.size()) {
			recommendations = recommendations.subList(0, limit);
		}
		return recommendations;
	}
}
