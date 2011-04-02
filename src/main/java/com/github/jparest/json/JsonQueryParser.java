package com.github.jparest.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.jparest.Attribute;
import com.github.jparest.QueryParser;
import com.github.jparest.Attribute.Type;

import flexjson.JSONTokener;


@Component
@Qualifier("json")
public class JsonQueryParser implements QueryParser {

	public static final String WHERE = "where";
	public static final String SELECT = "select";
	public static final String OP_PREFIX = "$";
	public static final String AND = OP_PREFIX + "and";
	public static final String OR = OP_PREFIX + "or";
	
	
	/**
	 * @see com.github.mitote.access.rest.QueryParser#parse(java.lang.String, javax.persistence.criteria.CriteriaBuilder, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T> CriteriaQuery<T> parse(String query, CriteriaBuilder builder, Class<?> entityClass) {
		CriteriaQuery<?> criteriaQuery = builder.createQuery();
		Root<?> root = criteriaQuery.from(entityClass);
		Context context = new Context(builder, root);
		
		Map<?,?> json = (Map<?,?>) new JSONTokener(query).nextValue();
		if (json.containsKey(SELECT)){
			criteriaQuery.multiselect( context.createSelection( json.get(SELECT) ) );
		}
		if (json.containsKey(WHERE)) {
			criteriaQuery.where( context.createPredicate( json.get(WHERE) ) );
		}
			
		return (CriteriaQuery<T>) criteriaQuery;
	}

	
	/**
	 * @see com.github.mitote.access.rest.QueryParser#parsePredicate(java.lang.String, javax.persistence.criteria.CriteriaBuilder, javax.persistence.criteria.CriteriaQuery)
	 */
	public Predicate parsePredicate(String query, CriteriaBuilder builder, CriteriaQuery<?> criteriaQuery) {
		Root<?> root = (Root<?>) criteriaQuery.getRoots().toArray()[0];
		Context context = new Context(builder, root);
		Map<?,?> json = (Map<?,?>) new JSONTokener(query).nextValue();
		if (json.containsKey(WHERE)) {
			json = (Map<?, ?>) json.get(WHERE);
		}
		return context.createPredicate(json);
	}
	

	public List<Attribute> parseSelect(String select) {
		return createAttributes( (List<?>) new JSONTokener(select).nextValue() );
	}
	
	
	public Map<?, ?> unmarshallEntity(String serializedEntity) {
		return (Map<?, ?>) new JSONTokener(serializedEntity).nextValue();
	}

	//---------------------------------------------------------------------------

	private List<Attribute> createAttributes(List<?> json) {
		List<Attribute> attributes = new ArrayList<Attribute>();
		for (Object item : json) {
			attributes.add( createAttribute(item) );
		}		
		return attributes;
	}
	
	
	private Attribute createAttribute(Object item) {
		if (item instanceof String) {
			return new Attribute( (String) item, Type.Any, null );
		}
		else if (item instanceof Map<?,?>) {
			Map<?, ?> map = (Map<?,?>) item;
			String path = (String) map.keySet().toArray()[ 0 ];
			Object value = map.get(path);
			if (value instanceof String) {
				return new Attribute( path, Type.valueOf((String) value), null );
			}
			else {
				//TODO
			}
		}
		return null;
	}


	private static class Context {
		private final CriteriaBuilder builder;
		private final Root<?> root;
		
		public Context(CriteriaBuilder builder, Root<?> root) {
			this.builder = builder;
			this.root = root;
		}		
		
		public List<Selection<?>> createSelection(Object select) {
			List<Selection<?>> selections = new ArrayList<Selection<?>>();
			
			if (select instanceof Collection) {
				Collection<?> json = (Collection<?>) select;
				for (Object selection : json) {
					if (selection instanceof String) {
						selections.add( root.get((String) selection) );
					}
				}
			}
			else if (select instanceof String) {
				selections.add( root.get((String) select) );
			}
			
			return selections;
		}
		
		
		public Predicate createPredicate(Object jsonPredicate) {
			if (jsonPredicate instanceof Collection) {
				return builder.and( createPredicateArray( jsonPredicate ) );
			}
			else if (jsonPredicate instanceof Map) {
				Map<?,?> json = (Map<?,?>) jsonPredicate;
				String key = (String) json.keySet().toArray()[ 0 ];
				
				if (AND.equals(key)) {
					return builder.and( createPredicateArray( json.get(key) ) );
				}
				else if (OR.equals(key)) {
					return builder.or( createPredicateArray( json.get(key) ) );
				}
				else if (!key.startsWith(OP_PREFIX)) {
					return createFieldPredicate( key, json.get(key) );
				}			
			}

			return null;
		}


		public Predicate[] createPredicateArray(Object jsonPredicates) {
			Collection<Predicate> predicates = new ArrayList<Predicate>();
			for (Object json : (Collection<?>) jsonPredicates) {
				predicates.add( createPredicate(json) );
			}
			return predicates.toArray( new Predicate[predicates.size()] );
		}
		
		
		public Predicate createFieldPredicate(String field, Object jsonValue) {
			Path<?> fieldPath = root.get(field);
			if (jsonValue instanceof Map) {
				//TODO
				return null;
			}
			else if (jsonValue instanceof Collection) {
				In<Object> in = builder.in(fieldPath);
				for (Object item : (Iterable<?>) jsonValue) {
					in.value(item);
				}
				return in;
			}
			else {
				return builder.equal(fieldPath, jsonValue);
			}			
		}
	}

}
