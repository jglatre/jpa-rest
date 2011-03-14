package com.github.jparest.json;

import static com.github.jparest.Attribute.Type.Any;
import static com.github.jparest.Attribute.Type.List;
import static com.github.jparest.Attribute.Type.Object;
import static com.github.jparest.Attribute.Type.Size;
import static com.github.jparest.Attribute.Type.String;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.jparest.Attribute;


public class JsonMarshallerTest {

	private JsonMarshaller strategy;

	
	@Before
	public void setup() {
		strategy = new JsonMarshaller();
		strategy.setPretty(false);
	}
	

	@Test 
	public void marshalAll() {
		String result = strategy.marshalObject( new Foo(), 
				asList(
						new Attribute("a", Any, null), 
						new Attribute("b", Any, null),
						new Attribute("bar", Object, asList(new Attribute("c", Any, null)))
						) );
		assertEquals("{\"a\":1,\"b\":\"x\",\"bar\":{\"c\":\"y\"}}", result);		
	}
	
	
	@Test
	public void marshalNested() {
		String result = strategy.marshalObject( new Foo(), 
				asList(	new Attribute("bar.c", String, null) ) );
		assertEquals("{\"bar\":{\"c\":\"y\"}}", result);		
	}
	
	
	@Test
	public void marshalIntToString() {
		String result = strategy.marshalObject( new Foo(), 
				asList(	new Attribute("a", String, null) ) );
		assertEquals("{\"a\":\"1\"}", result);
	}


	@Test
	public void marshalList() {
		String result = strategy.marshalObject( new Bar().getQuxes(), 
				asList(new Attribute("j", Any, null)) );
		assertEquals("[{\"j\":1},{\"j\":2},{\"j\":3}]", result);
	}
	
	
	@Test
	public void marshalNestedList() {
		String result = strategy.marshalObject( new Foo(), 
				asList(	new Attribute("bar.quxes", List, asList(new Attribute("j", Any, null))) ) );
		assertEquals("{\"bar\":{\"quxes\":[{\"j\":1},{\"j\":2},{\"j\":3}]}}", result);
	}
	
	
	@Test
	public void marshalListSize() {
		String result = strategy.marshalObject( new Bar(), 
				asList(	new Attribute("quxes", Size, null) ) );
		assertEquals("{\"quxes\":3}", result);
	}
	
	
	public static class Foo {
		public int getA() { return 1; }
		public String getB() { return "x"; }
		public Bar getBar() { return new Bar(); }
 	}
	
	public static class Bar {
		public String getC() { return "y"; }
		public List<Qux> getQuxes() { return asList( new Qux(1), new Qux(2), new Qux(3) ); }
	}
	
	public static class Qux {
		private long j;
		public Qux(long j) { this.j = j; }
		public long getJ() { return j; }
	}
	
}
