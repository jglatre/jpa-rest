package com.github.jparest.json;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.github.jparest.Attribute;
import com.github.jparest.Attribute.Type;


public class JsonQueryParserTest {

	@Mock private CriteriaBuilder builder;
	@Mock private CriteriaQuery<?> query;	
	@Mock private Root<?> root;
	
	private JsonQueryParser parser;
	
	public static class Foo {}
	
	
	@Before
	public void setup() {
		initMocks(this);
		when( builder.createQuery() ).thenReturn((CriteriaQuery<Object>) query);
		when( query.from(any(Class.class)) ).thenReturn(root);

		parser = new JsonQueryParser();
	}
	
	
	@Test
	public void parseWhereEqString() {		
		parser.parse("{where: {ip:'1.1.1.1'}}", builder, Foo.class);
		
		verify(builder).createQuery();
		verify(query).from(Foo.class);
		verify(query).where(any(Predicate.class));
		verify(root).get("ip");
		verify(builder).equal(any(Path.class), eq("1.1.1.1"));
	}
	
	
	@Test
	public void parseWhereAnd() {
		parser.parse("{where: {$and: [{agent:'local'}, {port:123}] }}", builder, Foo.class);
		
		verify(builder).createQuery();
		verify(query).from(Foo.class);
		verify(query).where(any(Predicate.class));
		verify(builder).and((Predicate[]) anyVararg());
		verify(root).get("agent");
		verify(builder).equal(any(Path.class), eq("local"));
		verify(root).get("port");
		verify(builder).equal(any(Path.class), eq(123));
	}
	
	
	@Test
	public void parseSelect() {
		parser.parse("{select: ['agent', 'ip']}", builder, Foo.class);
		
		verify(builder).createQuery();
		verify(query).from(Foo.class);
		verify(root).get("agent");
		verify(root).get("ip");
		verify(query).multiselect( anyList() );
		verify(query, never()).where( any(Expression.class) );
	}
	
	
	@Test 
	public void parseAttributes() {
		List<Attribute> attrs = parser.parseSelect("['agent', {'ip': 'Object'}]");
		assertEquals( 2, attrs.size() );
		assertEquals( "agent", attrs.get(0).getPath() );
		assertEquals( Type.Any, attrs.get(0).getType() );
		assertNull( attrs.get(0).getAttributes() );
		assertEquals( "ip", attrs.get(1).getPath() );
		assertEquals( Type.Object, attrs.get(1).getType() );
		assertNull( attrs.get(1).getAttributes() );
	}
}
