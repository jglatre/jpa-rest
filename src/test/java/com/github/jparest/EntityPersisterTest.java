package com.github.jparest;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.verification.VerificationMode;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.*;


public class EntityPersisterTest {

	@Mock private EntityManager entityManager;
	@Mock private Metamodel metamodel;
	@Mock private EntityType<?> entityType;
	
	private EntityPersister persister;
	
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		
		final SingularAttribute<?, ?> a = mock(SingularAttribute.class);
		when( a.getName() ).thenReturn("a");
		when( a.isId() ).thenReturn(true);
		when( a.getPersistentAttributeType() ).thenReturn(PersistentAttributeType.BASIC);
		
		when( entityManager.getMetamodel() ).thenReturn(metamodel);
		when( metamodel.entity(Foo.class) ).thenReturn((EntityType<Foo>) entityType);
		when( entityType.getSingularAttributes() ).thenAnswer( new Answer<Set<?>>() {
			public Set<?> answer(InvocationOnMock invocation) throws Throwable {
				return new HashSet<SingularAttribute<?, ?>>( Arrays.asList(a) );
			}			
		});
		
		persister = new EntityPersister();
		persister.setEntityManager(entityManager);
	}


	@Test
	public void updatePlain() {
		Foo foo = new Foo();
		when( entityManager.find(Foo.class, 1) ).thenReturn(foo);
		
		Map<String, Object> entity = new HashMap<String, Object>();
		entity.put("a", 1);
		entity.put("b", "xxx");
		
		persister.save(Foo.class, entity);
		
		assertEquals( "xxx", foo.getB() );
		
		verify( entityManager ).getMetamodel();
		verify( metamodel ).entity(Foo.class);
		verify( entityType, times(2) ).getSingularAttributes();
		verify( entityManager ).find( Foo.class, 1 );
		verify( entityManager ).merge(foo);
	}
	
	
	@Test
	public void insertPlain() {
		when( entityManager.find(Foo.class, 1) ).thenReturn(null);

		Map<String, Object> entity = new HashMap<String, Object>();
		entity.put("a", 1);
		entity.put("b", "xxx");
		
		persister.save(Foo.class, entity);
		
		verify( entityManager ).getMetamodel();
		verify( metamodel ).entity(Foo.class);
		verify( entityType, times(2) ).getSingularAttributes();
		verify( entityManager ).find( Foo.class, 1 );
		verify( entityManager ).persist( argThat( new ArgumentMatcher<Foo>() {
			public boolean matches(Object arg) {
				return ((Foo) arg).getA() == 1 && ((Foo) arg).getB().equals("xxx");
			}			
		}) );
	}

}
