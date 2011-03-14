package com.github.jparest.metadata;

import static javax.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_ONE;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_MANY;

import java.lang.reflect.AnnotatedElement;

import javax.persistence.metamodel.Attribute;


public class Field<T extends Attribute<?,?>> {
	
	protected final T attr;
	protected Constraints constraints;
	
	public Field(T attr) {
		this.attr = attr;
		this.constraints = new Constraints( (AnnotatedElement) attr.getJavaMember() );
	}

	public String getName() {
		return attr.getName();
	}

	public String getType() {
		return attr.getJavaType().getName();
	}
	
	public Constraints getConstraints() {
		return constraints;
	}


	public boolean isOneToMany() {
		return attr.isAssociation() ? attr.getPersistentAttributeType() == ONE_TO_MANY : false;
	}
		
	public boolean isManyToOne() {
		return attr.isAssociation() ? attr.getPersistentAttributeType() == MANY_TO_ONE : false;		
	}
}
