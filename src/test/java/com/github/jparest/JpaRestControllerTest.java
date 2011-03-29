package com.github.jparest;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.*;


public class JpaRestControllerTest {

	@Mock private EntityManager entityManager;
	@Mock private QueryParser queryParser;
	@Mock private Marshaller marshaller;
	@Mock private CriteriaBuilder builder;
	@Mock private CriteriaQuery<?> criteria;	
	@Mock private TypedQuery<?> query;
	
	private JpaRestController controller;
	

	@Before
	public void setUp() throws Exception {
		initMocks(this);
		
		controller = new JpaRestController();
		controller.setEntityManager(entityManager);
		controller.setDefaultDomainPackage( getClass().getPackage().getName() );
		controller.setQueryParser(queryParser);
		controller.setMarshaller(marshaller);
	}
	

	@Test
	public void count() {
		when( entityManager.getCriteriaBuilder() ).thenReturn(builder);
		when( builder.createQuery(any(Class.class)) ).thenReturn(criteria);
		when( entityManager.createQuery(any(CriteriaQuery.class)) ).thenReturn(query);
		when( ((TypedQuery<Long>) query).getSingleResult() ).thenReturn( 123L );
		when( marshaller.marshal( any(Response.class) ) ).thenReturn("{response}");
		
		assertEquals("{response}", controller.count("Foo", null));
		
		verify(criteria).from(Foo.class);
		verify(builder).count(any(Root.class));
		verify(criteria).select(any(Expression.class));
		verify(query).getSingleResult();
		verifyZeroInteractions(queryParser);
	}
	
	@Test
	public void countWithCriteria() {
		when( entityManager.getCriteriaBuilder() ).thenReturn(builder);
		when( builder.createQuery(Long.class) ).thenReturn((CriteriaQuery<Long>) criteria);
		when( entityManager.createQuery(any(CriteriaQuery.class)) ).thenReturn(query);
		when( ((TypedQuery<Long>) query).getSingleResult() ).thenReturn( 123L );
		when( marshaller.marshal( any(Response.class) ) ).thenReturn("{response}");
		
		assertEquals("{response}", controller.count("Foo", "{where: {a: 1}}"));
		
		verify(criteria).from(Foo.class);
		verify(builder).count(any(Root.class));
		verify(criteria).select(any(Expression.class));
		verify(queryParser).parsePredicate("{where: {a: 1}}", builder, criteria);
		verify(criteria).where(any(Predicate.class));
		verify(query).getSingleResult();
	}
	
	@Test
	public void countUnknownEntity() {
		Object count = controller.count("Xyz", null);

		verify(marshaller).marshal( any(ErrorResponse.class) );
//		assertTrue( count instanceof ResponseEntity );
//		assertEquals( HttpStatus.NOT_FOUND, ((ResponseEntity<?>) count).getStatusCode() );
		verifyZeroInteractions(entityManager);
	}
	

	@Test
	public void find() {
		final Foo foo = new Foo();
		when( entityManager.find(Foo.class, 123L) ).thenReturn( foo );
		when( marshaller.marshal( any(DataResponse.class) ) ).thenReturn("{foo}");
		
		assertEquals( "{foo}", controller.find("Foo", 123L, null) );
		
		verify(entityManager).find(Foo.class, 123L);
		verify(marshaller).marshal( any(DataResponse.class) );
		verifyZeroInteractions(queryParser);
	}
	
	@Test
	public void findWithUnknownEntity() {
		Object found = controller.find("Xyz", 123L, null);
		
		verify(marshaller).marshal( any(ErrorResponse.class) );
//		assertTrue( found instanceof ResponseEntity );
//		assertEquals( HttpStatus.NOT_FOUND, ((ResponseEntity<?>) found).getStatusCode() );
		verifyZeroInteractions(entityManager);
		verifyZeroInteractions(marshaller);
	}
	
	@Test
	@Ignore
	public void testList() {
		fail("Not yet implemented");
	}

	@Test
	public void getEntityMetadata() {
		final Metamodel metamodel = mock(Metamodel.class);
		final EntityType<Foo> type = mock(EntityType.class);
		when( entityManager.getMetamodel() ).thenReturn( metamodel );
		when( metamodel.entity(Foo.class) ).thenReturn( type );
		when( marshaller.marshal( any(ResultResponse.class) ) ).thenReturn("{foo}");
		
		assertEquals( "{foo}", controller.getMetadata("Foo") );
		
		verify(entityManager).getMetamodel();
		verify(metamodel).entity(Foo.class);
		verify(marshaller).marshal( any(ResultResponse.class) );
	}

	
	@Test
	public void getFieldMetadata() {
		final Metamodel metamodel = mock(Metamodel.class);
		final EntityType<Foo> type = mock(EntityType.class, Answers.RETURNS_MOCKS.get());
		when( entityManager.getMetamodel() ).thenReturn( metamodel );
		when( metamodel.entity(Foo.class) ).thenReturn( type );
		when( marshaller.marshal( any(ResultResponse.class) ) ).thenReturn("{foo.a}");
		
		assertEquals( "{foo.a}", controller.getMetadata("Foo", "a") );
		
		verify(entityManager).getMetamodel();
		verify(metamodel).entity(Foo.class);
		verify(type).getAttribute("a");
		verify(marshaller).marshal( any(ResultResponse.class) );
	}

}
