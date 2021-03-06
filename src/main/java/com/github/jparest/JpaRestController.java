package com.github.jparest;

import static com.github.jparest.ErrorResponse.ID_NOT_FOUND;
import static com.github.jparest.ErrorResponse.NOT_AN_ENTITY_NAME;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.jparest.metadata.Entity;
import com.github.jparest.metadata.Field;
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

	private EntityPersister entityPersister;
	
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
			@RequestParam(value = "criteria", required = false) String criteria
			) {
 		Response response = null;
 		
 		try {
			Class<?> entityClass = findEntityClass(className);
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Long> cq = builder.createQuery(Long.class);
			cq.select( builder.count( cq.from(entityClass) ) );
			
			if (criteria != null) {
				cq.where( queryParser.parsePredicate(criteria, builder, cq) );
			}
			 		
			long count = entityManager.createQuery(cq).getSingleResult();
			response = new ResultResponse<Long>( count );
		} 
 		catch (ClassNotFoundException e) {
 			response = new ErrorResponse(NOT_AN_ENTITY_NAME, className);
		}
 		
 		return marshaller.marshal(response);
    }

 	
 	@RequestMapping(method = GET, value = "{className}/{id}")
	@ResponseBody
 	public Object find(
 			@PathVariable String className, 
 			@PathVariable Long id,
 			@RequestParam(value = "select", required = false) String select
 			) {
 		Response response = null;
 		
 		try {
 			Class<?> entityClass = findEntityClass(className);
			Object entity = entityManager.find(entityClass, id);
			if (entity != null) {
				List<Attribute> attrs = Collections.emptyList();
				if (select != null) {
					attrs = queryParser.parseSelect(select);
				}
				response = new DataResponse<Object>( entity, attrs );
			}
			else {
				response = new ErrorResponse(ID_NOT_FOUND, id);
			}
		}
 		catch (ClassNotFoundException e) {
 			response = new ErrorResponse(NOT_AN_ENTITY_NAME, className);
 		}
 		
 		return marshaller.marshal(response);
 	}
 	
 	
 	@RequestMapping(method = GET, value = "{className}/list")
	@ResponseBody
 	public Object list(
 			@PathVariable String className,
 			@RequestParam(value = "criteria", required = false) String criteria,
 			@RequestParam(value = "page", required = false) Integer page, 
 			@RequestParam(value = "size", required = false) Integer size
 			) {
 		Response response = null;

 		try {
 			Class<?> entityClass = findEntityClass(className);
			TypedQuery<?> query = createQuery( criteria, entityClass, page, size );
 			List<?> result = query.getResultList();
 			List<Attribute> attrs = Collections.emptyList();  //TODO
 			response = new DataResponse<List<?>>( result, attrs );
		}
 		catch (ClassNotFoundException e) {
 			response = new ErrorResponse(NOT_AN_ENTITY_NAME, className);
 		}
 		
 		return marshaller.marshal(response);
 	}

 	
 	@RequestMapping(method = POST, value = "{className}/save")
 	@ResponseBody
 	public Object save(
 			@PathVariable String className,
 			@RequestBody String serializedEntity
 			) {
 		Response response = null;

 		log.debug("save(" + className + ", " + serializedEntity + ")");
 		
 		try {
 			Class<?> entityClass = findEntityClass(className);
 			Map<?, ?> mapEntity = queryParser.unmarshallEntity(serializedEntity);
 			Errors errors = getEntityPersister().save(entityClass, mapEntity);
 			// TODO 
		}
 		catch (ClassNotFoundException e) {
 			response = new ErrorResponse(NOT_AN_ENTITY_NAME, className);
 		}
 		
 		return marshaller.marshal(response); 		
 	}
 	
 	
	@RequestMapping(method = GET, value = "metadata")
	@ResponseBody
	public Object getMetadata() {
		Response response = new ResultResponse<List<String>>( getMetamodel().getEntityNames() );
		return marshaller.marshal(response);
	}

 	
	@RequestMapping(method = GET, value = "metadata/{className}")
	@ResponseBody
	public Object getMetadata(
			@PathVariable String className
			) {
		Response response;
		
		try {
			Class<?> entityClass = findEntityClass(className);
			response = new ResultResponse<Entity>( getMetamodel().getEntity(entityClass) );
		}
		catch (ClassNotFoundException e) {
			response = new ErrorResponse(NOT_AN_ENTITY_NAME, className);
		}
		
 		return marshaller.marshal(response);
	}
	
 	
	@RequestMapping(method = GET, value = "metadata/{className}/{field}")
	@ResponseBody
	public Object getMetadata(
			@PathVariable String className,
			@PathVariable String field
			) {
		Response response;
		
		try {
			Class<?> entityClass = findEntityClass(className);
			Entity entity = getMetamodel().getEntity(entityClass);
			response = new ResultResponse<Field<?>>( entity.getField(field) );
		}
		catch (ClassNotFoundException e) {
			response = new ErrorResponse(NOT_AN_ENTITY_NAME, className);
		}
		
 		return marshaller.marshal(response);
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

 	
 	protected EntityPersister getEntityPersister() {
 		if (entityPersister == null) {
			entityPersister = new EntityPersister( entityManager );
		}
		return entityPersister;
 	}
 	
 	
 	protected MetamodelWrapper getMetamodel() {
 		if (metamodel == null) {
			metamodel = new MetamodelWrapper( entityManager.getMetamodel() );
		}
		return metamodel;
 	}
 	
}
