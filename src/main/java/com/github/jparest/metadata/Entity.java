package com.github.jparest.metadata;

import java.util.Map;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.util.CachingMapDecorator;


public class Entity {

	private final EntityType<?> type;
	private Map<String, Field<?>> fields = new FieldMap();
	
	
	public Entity(EntityType<?> type) {
		this.type = type;
	}
	
	
	public String getName() {
		return type.getName();
	}
	
	public String getType() {
		return type.getJavaType().getName();
	}
	
	public String getSupertype() {
		IdentifiableType<?> supertype = type.getSupertype();
		return supertype != null ? supertype.getJavaType().getName() : null;
	}
	
	public String getIdType() {
		return type.getIdType().getJavaType().getName();
	}
	
	
	public Map<String, Field<?>> getFields() {
		fillFieldsMap();
		return fields;
	}
	
	
	public Field<?> getField(String name) {
		return fields.get(name);
	}
	
	//-----------------------------------------------------------------------------
	
	private final class FieldMap extends CachingMapDecorator<String, Field<?>> {
		@Override
		protected Field<?> create(String key) {
			Attribute<?, ?> attribute = type.getAttribute(key);
			if (attribute instanceof SingularAttribute) {
				return new SingularField( (SingularAttribute<?, ?>) attribute );				
			}
			else if (attribute instanceof PluralAttribute) {
				return new PluralField( (PluralAttribute<?, ?, ?>) attribute );
			}
			else {
				return new Field<Attribute<?,?>>( attribute );
			}
		}
	}

	
	private void fillFieldsMap() {
		for (Attribute<?, ?> attr : type.getAttributes()) {
			fields.get( attr.getName() );
		}
	}
}
