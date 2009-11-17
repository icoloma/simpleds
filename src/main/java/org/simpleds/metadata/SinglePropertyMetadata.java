package org.simpleds.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.simpleds.converter.Converter;

/**
 * A single property
 * @author icoloma
 */
public class SinglePropertyMetadata implements PropertyMetadata {

	/** property name */
	private String name;
	
	/** the getter method */
	private Method getter;
	
	/** the setter method */
	private Method setter;
	
	/** the field used for direct access, if there is none of the above */
	private Field field;
	
	/** the converter used to convert values between java and the Google datastore */
	private Converter converter;
	
	/** the type  of this property object */
	private Class<?> propertyType;
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " { name=" + name + 
			", getter=" + (getter == null? null : getter.getName()) + 
			", setter=" + (setter == null? null : setter.getName()) + 
			", field=" + (field == null? null : field.getName()) + 
			", converter=" + converter + " }";
	}
	
	public Object getValue(Object container) {
		try {
			return getter != null? getter.invoke(container) : field.get(container);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} 
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		T annotation = getter == null? null : getter.getAnnotation(annotationClass);
		if (annotation == null && field != null) {
			annotation = field.getAnnotation(annotationClass);
		}
		return annotation;
	}
	
	public void setValue(Object container, Object value) {
		try {
			if (setter != null) {
				setter.invoke(container, value);
			} else {
				field.set(container, value);
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} 
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Method getGetter() {
		return getter;
	}

	public void setGetter(Method getter) {
		this.getter = getter;
	}

	public Method getSetter() {
		return setter;
	}

	public void setSetter(Method setter) {
		this.setter = setter;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	public Class<?> getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(Class<?> propertyType) {
		this.propertyType = propertyType;
	}
	
}
