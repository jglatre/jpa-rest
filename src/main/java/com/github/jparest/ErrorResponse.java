package com.github.jparest;

/**
 * A not OK response, with an error code and optional arguments.
 * 
 * @author juanjo
 */
public class ErrorResponse extends Response {

	public static final String ID_NOT_FOUND = "jparest.IdNotFound";
	public static final String NOT_AN_ENTITY_NAME = "jparest.NotAnEntityName";

	private final String error;
	private final Object[] args;
	
	
	public ErrorResponse(String error, Object... args) {
		super(false);
		this.error = error;
		this.args = args;
	}
	
	
	public String getError() {
		return error;
	}
	
	
	public Object[] getArgs() {
		return args;
	}
	
}
