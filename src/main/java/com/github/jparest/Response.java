package com.github.jparest;

import java.io.Serializable;


/**
 * Holds contents of any successful (i.e. 200 HTTP code) response.
 * 
 * @author juanjo
 */
public abstract class Response implements Serializable {

	private final boolean ok;
	
	
	public Response(boolean ok) {
		this.ok = ok;
	}
	
	
	public boolean isOk() {
		return ok;
	}
}
