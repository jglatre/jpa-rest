package com.github.jparest;


/**
 * An OK response with some attached result.
 * 
 * @author juanjo
 * @param <T> result type.
 */
public class ResultResponse<T> extends Response {

	private final T result;
	
	
	public ResultResponse(T result) {
		super(true);
		this.result = result;
	}
	
	
	public T getResult() {
		return result;
	}

}
