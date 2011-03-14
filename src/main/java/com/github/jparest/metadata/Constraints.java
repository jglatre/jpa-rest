package com.github.jparest.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import com.github.jparest.metadata.Constraints.Constraint;


public class Constraints extends HashMap<String, Constraint<?>>{

	public Constraints(AnnotatedElement javaMember) {
		if (javaMember.isAnnotationPresent(Size.class)) {
			put( "size", new SizeConstraint(javaMember.getAnnotation(Size.class)) );
		}
		if (javaMember.isAnnotationPresent(Min.class)) {
			put( "min", new MinConstraint(javaMember.getAnnotation(Min.class)) );
		}
		if (javaMember.isAnnotationPresent(Max.class)) {
			put( "max", new MaxConstraint(javaMember.getAnnotation(Max.class)) );
		}
	}

	
	public static abstract class Constraint<T extends Annotation> {
		protected final T annotation;

		public Constraint(T annotation) {
			this.annotation = annotation;
		}			
	}
	
	
	public static class SizeConstraint extends Constraint<Size> {		
		public SizeConstraint(Size size) {
			super(size);
		}

		public int getMin() {
			return annotation.min();
		}

		public int getMax() {
			return annotation.max();
		}
		
		public String getMessage() {
			return annotation.message();
		}
	}
	
	
	public static class MinConstraint extends Constraint<Min> {
		public MinConstraint(Min min) {
			super(min);
		}
		
		public long getValue() {
			return annotation.value();
		}
		
		public String getMessage() {
			return annotation.message();
		}
	}
	
	
	public static class MaxConstraint extends Constraint<Max> {
		public MaxConstraint(Max max) {
			super(max);
		}
				
		public long getValue() {
			return annotation.value();
		}
		
		public String getMessage() {
			return annotation.message();
		}
	}
}