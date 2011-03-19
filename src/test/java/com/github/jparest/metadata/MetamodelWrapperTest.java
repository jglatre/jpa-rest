package com.github.jparest.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.github.jparest.Foo;


public class MetamodelWrapperTest {

	@Mock private Metamodel metamodel;

	private MetamodelWrapper wrapper;
	
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		final EntityType<Foo> foo = mock(EntityType.class);
		when( foo.getName() ).thenReturn("foo");
		when( foo.getJavaType() ).thenReturn(Foo.class);
		when( metamodel.entity(Foo.class) ).thenReturn(foo);
		when( metamodel.getEntities() ).thenReturn( new HashSet<EntityType<?>>(Arrays.asList(foo)) );
		
		wrapper = new MetamodelWrapper(metamodel);
	}

	
	@Test
	public void getEntityNames() {
		final List<String> names = wrapper.getEntityNames();
		assertEquals( 1, names.size() );
		assertEquals( "foo", names.get(0) );
		verify(metamodel).getEntities();
		verify(metamodel, never()).entity(any(Class.class));
	}

	
	@Test
	public void getEntities() {
		Map<Class<?>, Entity> entities = wrapper.getEntities();
		assertEquals( 1, entities.size() );
		assertTrue( entities.containsKey(Foo.class) );
		verify(metamodel).getEntities();
		verify(metamodel).entity(Foo.class);
	}

	
	@Test
	public void getEntity() {
		final Entity entity = wrapper.getEntity(Foo.class);
		assertEquals( "foo", entity.getName() );
		verify(metamodel).entity(Foo.class);
	}

}
