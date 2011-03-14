package com.github.jparest;

import java.util.List;


public class Attribute {

	public enum Type {
		Any,
		String,
		Id,
		Object,
		Size,
		List
	}
	
	private final String path;
	private final Type type;
	private final List<Attribute> attributes;
	
	public Attribute(String path, Type type, List<Attribute> attributes) {
		this.path = path;
		this.type = type;
		this.attributes = attributes;
	}

	public String getPath() {
		return path;
	}

	public Type getType() {
		return type;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}
	
}
