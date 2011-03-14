package com.github.jparest.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.springframework.util.CachingMapDecorator;


/**
 * Wrapper for exporting a JPA Metamodel as a REST response.
 * 
 * @author juanjo
 */
public class Model {

	private final Metamodel metamodel;

	private Map<Class<?>, Entity> entities = new CachingMapDecorator<Class<?>, Entity>() {
		@Override
		protected Entity create(Class<?> key) {
			return new Entity( metamodel.entity(key) );
		}
	};
	
	
	public Model(Metamodel metamodel) {
		this.metamodel = metamodel;
	}
	
	
	public List<String> getEntityNames() {
		List<String> names = new ArrayList<String>();
		for (EntityType<?> type : metamodel.getEntities()) {
			names.add( type.getName() );
		}
		return names;
	}
	
	
	public Map<Class<?>, Entity> getEntities() {
		fillEntitiesMap();
		return entities;
	}
	
	
	public Entity getEntity(Class<?> entityClass) {
		return entities.get(entityClass);
	}
	
	//--------------------------------------------------------------------------
	
	private void fillEntitiesMap() {
		for (EntityType<?> type : metamodel.getEntities()) {
			entities.get( type.getJavaType() );
		}
	}
}
