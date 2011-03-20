package com.github.jparest;

import java.util.List;



public interface Marshaller {

	String marshal(Response response);
	
	@Deprecated
    String marshalObject(Object item, List<Attribute> attributes);
}
