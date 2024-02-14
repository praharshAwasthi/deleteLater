package uk.tw.energy.exceptions;

public class InvalidMeterIdException extends RuntimeException {

	private static final long serialVersionUID = 3768540078707467955L;
	
	public InvalidMeterIdException(String errorMesssage) {
		super(errorMesssage);
	}
}
