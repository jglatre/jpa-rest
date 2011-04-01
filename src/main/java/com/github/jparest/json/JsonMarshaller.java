package com.github.jparest.json;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.jparest.Attribute;
import com.github.jparest.DataResponse;
import com.github.jparest.ErrorResponse;
import com.github.jparest.Marshaller;
import com.github.jparest.Response;
import com.github.jparest.ResultResponse;

import flexjson.JSONSerializer;
import flexjson.transformer.AbstractTransformer;


@Component
@Qualifier("json")
public class JsonMarshaller implements Marshaller {

	private boolean pretty = true;
	
	
	public String marshal(Response response) {
		JSONSerializer serializer = new JSONSerializer().exclude("*.class").prettyPrint(pretty);
		serializer.include("ok");

		if (response instanceof ResultResponse) {
			serializer.include("result");

			if (response instanceof DataResponse) {
				DataResponse<?> dataResponse = (DataResponse<?>) response;
				List<Attribute> attributes = dataResponse.getAttributes();
				if (attributes != null && !attributes.isEmpty()) {
					configureSerializer( serializer, "result", attributes );
				}
			}
		}
		else if (response instanceof ErrorResponse) {
			serializer.include("error", "args");
		}
		
		return serializer.serialize(response);
	}

	
	@Override
	public String marshalObject(Object item, List<Attribute> attributes) {
		return createSerializer(attributes).serialize(item);
	}
	
	
	public void setPretty(boolean pretty) {
		this.pretty = pretty;
	}
	
	//--------------------------------------------------------------------------------
	
	protected JSONSerializer createSerializer(List<Attribute> attributes) {
		JSONSerializer serializer = new JSONSerializer().exclude("*.class").prettyPrint(pretty);
		if (attributes != null && !attributes.isEmpty()) {
			configureSerializer(serializer, null, attributes);
		}
		return serializer;
	}
	
	
	private void configureSerializer(JSONSerializer serializer, String root, List<Attribute> attributes) {
		for (Attribute attribute : attributes) {
			String path = root == null ? attribute.getPath() : root + '.' + attribute.getPath();
			serializer.include(path);

			switch (attribute.getType()) {
			case Object:
			case List:
				configureSerializer( serializer, path, attribute.getAttributes() );
				break;
			case String:
				serializer.transform( new StringTransformer(), path );
				break;
			case Size:
				serializer.transform( new SizeTransformer(), path );
				break;
			}
		}
		serializer.exclude("*");
	}
	
	
	public static class StringTransformer extends AbstractTransformer {
	    public void transform(Object object) {
	        getContext().writeQuoted( object != null ? object.toString() : "" );
	    }
	}

	
	public static class SizeTransformer extends AbstractTransformer {
		public void transform(Object object) {
			int size = -1;
			if (object instanceof Collection) {
				size = ((Collection<?>) object).size();
			}
			else if (object.getClass().isArray()) {
				size = Array.getLength(object);
			}
			else if (object instanceof CharSequence) {
				size = ((CharSequence) object).length();
			}
			getContext().write( String.valueOf(size) );
		}		
	}
}
