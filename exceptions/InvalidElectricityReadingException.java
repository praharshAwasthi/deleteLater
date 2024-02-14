package uk.tw.energy.exceptions;

public class InvalidElectricityReadingException extends RuntimeException{

	private static final long serialVersionUID = -3021101691249432105L;
	
	public InvalidElectricityReadingException(String errorMessage) {
		super(errorMessage);
	}

}
