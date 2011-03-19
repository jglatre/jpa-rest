package com.github.jparest;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.jparest.metadata.MetamodelWrapper;


@RequestMapping(value="/jparest/**", headers="Accept=application/json")
@Controller
public class JpaRestController {

	private static final Log log = LogFactory.getLog(JpaRestController.class);
	
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired 
	private QueryParser queryParser; 

	@Autowired 
	private Marshaller marshaller;
	
	private String defaultDomainPackage;

	private MetamodelWrapper metamodel;
	
	
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	
	public void setQueryParser(QueryParser queryParser) {
		this.queryParser = queryParser;
	}
	
	
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	
	
	@Value("${jparest.defaultDomainPackage}")
	public void setDefaultDomainPackage(String defaultDomainPackage) {
		this.defaultDomainPackage = defaultDomainPackage;
		if (StringUtils.hasLength(this.defaultDomainPackage) && !this.defaultDomainPackage.endsWith(".")) {
			this.defaultDomainPackage += ".";
		}
	}

	
 	@RequestMapping(method = GET, value = "{className}/count")
	@ResponseBody
	public Object count(
			@PathVariable String className,
			@RequestParam(value = "criteria", required = false) String criteria) {

 		try {
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
 		catch (ClassNotFoundException e) {
			return new ResponseEntity<String>("Unknown entity class name: " + className, NOT_FOUND);
		}
    }

 	
 	@RequestMapping(method = GET, value = "{className}/{id}")
	@ResponseBody
 	public Object find(
 			@PathVariable String className, 
 			@PathVariable Long id,
 			@RequestParam(value = "select", required = false) String select
 			) {
 		
 		try {
 			Class<?> entityClass = findEntityClass(className);
			Object entity = entityManager.find(entityClass, id);
			if (entity != null) {
				List<Attribute> attrs = Collections.emptyList();
				if (select != null) {
					attrs = queryParser.parseSelect(select);
				}
				return marshaller.marshalObject( entity, attrs );
			}
			else {
				return new ResponseEntity<String>("Unknown id: " + id, NOT_FOUND);
			}
		}
 		catch (ClassNotFoundException e) {
			return new ResponseEntity<String>("Unknown entity class name: " + className, NOT_FOUND);
 		}
 		catch (Exception e) {
			log.error("Unable to fulfill 'find' request: className=" + className, e);
			return new ResponseEntity<String>( e.getMessage(), INTERNAL_SERVER_ERROR );
 		}
 	}
 	
 	
 	@RequestMapping(method = GET, value = "{className}/list")
	@ResponseBody
 	public Object list(
 			@PathVariable String className,
 			@RequestParam(value = "criteria", required = false) String criteria,
 			@RequestParam(value = "page", required = false) Integer page, 
 			@RequestParam(value = "size", required = false) Integer size
 			) {

 		try {
 			Class<?> entityClass = findEntityClass(className);
			TypedQuery<?> query = createQuery( criteria, entityClass, page, size );
 			List<?> result = query.getResultList();
 			return marshaller.marshalObject(result, null);
		}
 		catch (ClassNotFoundException e) {
 			return new ResponseEntity<String>(NOT_FOUND);
 		}
 	}
 	
 	
	@RequestMapping(method = GET, value = "metadata")
	@ResponseBody
	public Object getMetadata() {
		try {
			return marshaller.marshalObject( getMetamodel().getEntityNames(), null );
		} 
		catch (Exception e) {
			log.error("Unable to fulfill metadata request" , e);
			return new ResponseEntity<String>(INTERNAL_SERVER_ERROR);		
		}
	}

 	
	@RequestMapping(method = GET, value = "metadata/{className}")
	@ResponseBody
	public Object getMetadata(
			@PathVariable String className) {
		
		try {
			Class<?> entityClass = findEntityClass(className);
			return marshaller.marshalObject( getMetamodel().getEntity(entityClass), null );
		}
		catch (ClassNotFoundException e) {
			return new ResponseEntity<String>(NOT_FOUND);		
		}
		catch (Exception e) {
			log.error("Unable to fulfill metadata request: className=" + className, e);
			return new ResponseEntity<String>(INTERNAL_SERVER_ERROR);
		}
	}
	
 	
	@RequestMapping(method = GET, value = "metadata/{className}/{field}")
	@ResponseBody
	public Object getMetadata(
			@PathVariable String className,
			@PathVariable String field
			) {
		
		try {
			Class<?> entityClass = findEntityClass(className);
			return marshaller.marshalObject( getMetamodel().getEntity(entityClass).getField(field), null );
		}
		catch (ClassNotFoundException e) {
			return new ResponseEntity<String>(NOT_FOUND);		
		}
		catch (Exception e) {
			log.error("Unable to fulfill metadata request: className=" + className, e);
			return new ResponseEntity<String>(INTERNAL_SERVER_ERROR);
		}
	}
	
 	
 	protected Class<?> findEntityClass(String className) throws ClassNotFoundException {
		String fullClassName = className.contains(".") ? className : defaultDomainPackage + className;
		return ClassUtils.forName( fullClassName, null );
 	}
 	
 	
 	protected TypedQuery<?> createQuery(String criteria, Class<?> entityClass, Integer page, Integer size) {
		TypedQuery<?> query;

		if (StringUtils.hasLength(criteria)) {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<?> criteriaQuery = queryParser.parse(criteria, builder, entityClass);
			query = entityManager.createQuery(criteriaQuery);
		}
		else {
			query = entityManager.createQuery("select o from " + entityClass.getSimpleName() + " o", entityClass);
		} 
		
		if (page != null || size != null) {
			int max = size == null ? 10 : size.intValue();
 			int first = page == null ? 0 : (page.intValue() - 1) * max;
 			query = query.setFirstResult(first).setMaxResults(max);
 		}
		
		return query;
 	}

 	
 	protected MetamodelWrapper getMetamodel() {
 		if (metamodel == null) {
			metamodel = new MetamodelWrapper( entityManager.getMetamodel() );
		}
		return metamodel;
 	}
}
