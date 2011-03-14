package com.github.jparest.metadata;

import javax.persistence.metamodel.SingularAttribute;


public class SingularField extends Field<SingularAttribute<?,?>> {

	public SingularField(SingularAttribute<?, ?> attr) {
		super(attr);
	}


	public boolean isIdentity() {
		return attr.isId();
	}
	
	public boolean isOptional() {
		return attr.isOptional();
	}
}
