package uk.tw.energy.exceptions;

/**
 * Exception when the number of recommendations required exceed the total number of recommendations possible
 * Assumption - If the value of limit>recommendations.size() throw an exception to notify the user.
 */
public class RecommendationLimitExccededException extends RuntimeException {
	
	private static final long serialVersionUID = 2349816364592058911L;
	
	public RecommendationLimitExccededException(String errorMessage) {
		super(errorMessage);
	}
	

}
