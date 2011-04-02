package com.github.jparest;

import static javax.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_ONE;
import static javax.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_ONE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.util.CachingMapDecorator;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;


/**
 * A helper class for saving maps as entities.
 * 
 * @author juanjo
 */
public class EntityPersister {

	@PersistenceContext
	private EntityManager entityManager;

	private Map<Class<?>, EntityMetadata> metadata = new CachingMapDecorator<Class<?>, EntityMetadata>() {
		protected EntityMetadata create(Class<?> key) {
			return new EntityMetadata(key);
		}
	};
	
	
	public EntityPersister() {
	}
	
	
	public EntityPersister(EntityManager entityManager) {
		setEntityManager(entityManager);
	}
	
	
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	
	/**
	 * Save a map representation of an entity.
	 * 
	 * @param entityClass class of the entity to save.
	 * @param mapEntity field values.
	 * @return
	 */
	public Errors save(Class<?> entityClass, Map<?,?> mapEntity) {
		MutablePropertyValues values = new MutablePropertyValues(mapEntity);
		Errors errors = new MapBindingResult(Collections.emptyMap(), null);
		save( entityClass, values, errors );
		return errors;
	}
	
	//--------------------------------------------------------------------
	
 	private Object save(Class<?> entityClass, MutablePropertyValues entityMap, Errors errors) {
 		EntityMetadata entityMetadata = metadata.get(entityClass);

 		resolveReferences(entityMetadata, entityMap, errors);
 		
 		if (errors.hasErrors()) {
 			return null;
 		}
 		
		Object oldEntity = null;
		String idName = entityMetadata.getIdName();
 		
 		if (entityMap.contains(idName)) {
			Object idValue = entityMap.getPropertyValue(idName).getValue();
			oldEntity = entityManager.find( entityClass, idValue );
 		}
 		
		if (oldEntity != null) {
			BindingResult result = bind( oldEntity, entityMap, idName );
			if (!result.hasErrors()) {
				entityManager.merge( oldEntity );
			}
			else {
				errors.addAllErrors( result );
			}
			return oldEntity;
		}
		else {
	 		Object entity = BeanUtils.instantiateClass(entityClass);
			BindingResult result = bind( entity, entityMap, idName );
			if (!result.hasErrors()) {
				entityManager.persist(entity);
			}
			else {
				errors.addAllErrors( result );
			}
	 		return entity;
		}
 	}


 	private BindingResult bind(Object entity, MutablePropertyValues values, String idName) {
		DataBinder binder = new DataBinder(entity);
		binder.initDirectFieldAccess();
		binder.setDisallowedFields(idName);
		binder.bind(values);
		return binder.getBindingResult();
 	}
 	
 	
 	private Object resolveReferences(EntityMetadata entityMetadata, MutablePropertyValues entity, Errors errors) {
 		for (SingularAttribute<?,?> attribute : entityMetadata.getReferenceAttributes()) {
 			String name = attribute.getName();
			if (entity.contains(name)) {
				Object value = entity.getPropertyValue(name);
		 		Class<?> refType = attribute.getJavaType();
				Object refEntity;
		 		
		 		if (value instanceof Map) {
		 			MutablePropertyValues values = new MutablePropertyValues((Map<?,?>) value);
					refEntity = save(refType, values, errors);
		 		}
		 		else {
		 			refEntity = value != null ? entityManager.find(refType, value) : null;
		 		}
				entity.add( name, refEntity );
 			}
 		}
 		return entity;
 	}

 	
 	private class EntityMetadata {
 		private final EntityType<?> entityType;
 		private String idName;
 		private List<SingularAttribute<?,?>> referenceAttributes;
 		
 		public EntityMetadata(Class<?> entityClass) {
 			this.entityType = entityManager.getMetamodel().entity(entityClass);
 		}

 		public String getIdName() {
 			if (idName == null) {
	 	 		for (SingularAttribute<?, ?> attribute : entityType.getSingularAttributes()) {
	 	 			if (attribute.isId()) {
	 	 				idName = attribute.getName();
	 	 			}
	 	 		}
 			}
 	 		return idName;
 	 	}

 	 	public List<SingularAttribute<?,?>> getReferenceAttributes() {
 	 		if (referenceAttributes == null) {
	 	 		referenceAttributes = new ArrayList<SingularAttribute<?,?>>();
	 	 		for (SingularAttribute<?,?> attribute : entityType.getSingularAttributes()) {
	 	 			PersistentAttributeType persistentType = attribute.getPersistentAttributeType();
	 	 			if (persistentType == MANY_TO_ONE || persistentType == ONE_TO_ONE) {
	 	 				referenceAttributes.add( attribute );
	 	 			}
	 	 		}
 	 		}
 	 		return referenceAttributes;
 	 	}
 	}

}
