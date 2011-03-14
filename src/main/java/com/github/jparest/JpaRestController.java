package com.github.jparest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.jparest.json.JsonMarshallingStrategy;
import com.github.jparest.json.JsonQueryParser;
import com.github.jparest.metadata.Model;


@RequestMapping(value="/jparest/**", headers="Accept=application/json")
@Controller
public class JpaRestController {

	private static final String DOMAIN_PACKAGE = "com.github.mitote.access";
	
	@PersistenceContext
	private EntityManager entityManager;

	private QueryParser queryParser = new JsonQueryParser();          	//TODO inject
	private Marshaller marshaller = new JsonMarshallingStrategy();		//TODO inject
	
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	
 	@RequestMapping(method = GET, value = "{className}/count")
	@ResponseBody
	public String count(
			@PathVariable String className,
			@RequestParam(value = "criteria", required = false) String criteria) {

 		Class<?> entityClass = findEntityClass(className);
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = builder.createQuery(Long.class);
 		cq.select( builder.count( cq.from(entityClass) ) );
 		
 		if (criteria != null) {
 			cq.where( queryParser.parsePredicate(criteria, builder, cq) );
 		}
 		 		
 		long count = entityManager.createQuery(cq).getSingleResult();
 		return String.valueOf(count);
    }

 	
 	@RequestMapping(method = GET, value = "{className}/{id}")
	@ResponseBody
 	public Object find(
 			@PathVariable String className, 
 			@PathVariable Long id,
 			@RequestParam(value = "select", required = false) String select
 			) {
 		
		Class<?> entityClass = findEntityClass(className);
		if (entityClass != null) {
			Object entity = entityManager.find(entityClass, id);
			if (entity != null) {
				List<Attribute> attrs = Collections.emptyList();
				if (select != null) {
					attrs = queryParser.parseSelect(select);
				}
				return marshaller.marshalObject( entity, attrs );
			}
		}
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
 	}
 	
 	
 	@RequestMapping(method = GET, value = "{className}/list")
	@ResponseBody
 	public Object list(
 			@PathVariable String className,
 			@RequestParam(value = "criteria", required = false) String criteria,
 			@RequestParam(value = "page", required = false) Integer page, 
 			@RequestParam(value = "size", required = false) Integer size
 			) {

		Class<?> entityClass = findEntityClass(className);
		if (entityClass != null) {
			TypedQuery<?> query;

			if (criteria != null) {
				CriteriaBuilder builder = entityManager.getCriteriaBuilder();
				CriteriaQuery<?> criteriaQuery = queryParser.parse(criteria, builder, entityClass);
				query = entityManager.createQuery(criteriaQuery);
			}
			else {
				query = entityManager.createQuery("select o from " + className + " o", entityClass);
			}
			
			if (page != null || size != null) {
				int max = size == null ? 10 : size.intValue();
	 			int first = page == null ? 0 : (page.intValue() - 1) * max;
	 			query = query.setFirstResult(first).setMaxResults(max);
	 		}

 			List<?> result = query.getResultList();
 			return marshaller.marshalObject(result, null);
		}
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
 	}
 	

	@RequestMapping(method = GET, value = "metadata")
	@ResponseBody
	public Object getMetadata() {
		try {
			Model model = new Model(entityManager.getMetamodel());
			return marshaller.marshalObject(model.getEntityNames(), null);
		} 
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);		
		}
	}

 	
	@RequestMapping(method = GET, value = "metadata/{className}")
	@ResponseBody
	public Object getMetadata(
			@PathVariable String className) {
		
		Class<?> entityClass = findEntityClass(className);
		if (entityClass != null) {
			Model model = new Model(entityManager.getMetamodel());
			return marshaller.marshalObject( model.getEntity(entityClass), null );
		}
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);		
	}
	
 	
	@RequestMapping(method = GET, value = "metadata/{className}/{field}")
	@ResponseBody
	public Object getMetadata(
			@PathVariable String className,
			@PathVariable String field
			) {
		
		try {
			Class<?> entityClass = findEntityClass(className);
			Model model = new Model(entityManager.getMetamodel());
			return marshaller.marshalObject( model.getEntity(entityClass).getField(field), null );
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);		
		}
	}
	
 	
 	protected Class<?> findEntityClass(String className) {
 		try {
			return ClassUtils.forName( DOMAIN_PACKAGE + "." + className, null );
		} 
 		catch (ClassNotFoundException e) {
 			return null;
		}
 		catch (LinkageError e) {
 			return null;
 		}
 	}
 	
}
