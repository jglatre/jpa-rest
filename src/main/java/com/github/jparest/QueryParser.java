package com.github.jparest;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;


/**
 * Builds JPA queries from String representation.
 * 
 * @author juanjo
 */
public interface QueryParser {

	<T> CriteriaQuery<T> parse(String query, CriteriaBuilder builder, Class<?> entityClass);

	Predicate parsePredicate(String query, CriteriaBuilder builder,	CriteriaQuery<?> criteriaQuery);
	
	List<Attribute> parseSelect(String select);

}
