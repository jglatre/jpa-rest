package com.github.jparest;

import java.util.List;


/**
 * A Response including a complex data structure, in which
 * some attributes may be selected.
 * 
 * @author juanjo
 */
public class DataResponse<T> extends ResultResponse<T> {

	private final List<Attribute> attributes;
	
	
	public DataResponse(T result, List<Attribute> attributes) {
		super(result);
		this.attributes = attributes;
	}

	
	public List<Attribute> getAttributes() {
		return attributes;
	}
}
