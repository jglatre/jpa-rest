package com.github.jparest;

import java.util.List;



public interface Marshaller {

    String marshalObject(Object item, List<Attribute> attributes);
}
