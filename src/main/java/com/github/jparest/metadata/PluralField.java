package com.github.jparest.metadata;

import javax.persistence.metamodel.PluralAttribute;


public class PluralField extends Field<PluralAttribute<?, ?, ?>> {
	
	public PluralField(PluralAttribute<?,?,?> attr) {
		super(attr);
	}

	public String getCollectionType() {
		return attr.getCollectionType().toString();
	}
	
	public String getElementType() {
		return attr.getElementType().getJavaType().getName();
	}

}
