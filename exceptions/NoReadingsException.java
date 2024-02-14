package uk.tw.energy.exceptions;

/**
 * Exception when we try to recommend plans or calculate costs
 * without any readings for that meter. 
 * Assumption - If the user has not recorded any readings its not possible to compare different plans
 * or calculate costs for each plan.
 */
public class NoReadingsException extends RuntimeException {
	
	private static final long serialVersionUID = -8570961354762047484L;

	public NoReadingsException(String errorMessage) {
		super(errorMessage);
	}
}
