package org.simpleds.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.simpleds.converter.CollectionConverter;
import org.simpleds.converter.Converter;
import org.simpleds.util.ClassUtils;

/**
 * A single property
 * @author icoloma
 */
public class SinglePropertyMetadata<J, D> extends AbstractPropertyMetadata<J, D> {

	/** property name */
	private String name;
	
	/** the getter method */
	private Method getter;
	
	/** the setter method */
	private Method setter;
	
	/** the field used for direct access, if there is none of the above */
	private Field field;
	
	/** the converter used to convert values between java and the Google datastore */
	private Converter<J, D> converter;
	
	/** the type  of this property object */
	private Class<J> propertyType;
	
	/** true if this property is indexed, false otherwise */
	private boolean indexed = true;
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " { name=" + name + 
			", getter=" + (getter == null? null : getter.getName()) + 
			", setter=" + (setter == null? null : setter.getName()) + 
			", field=" + (field == null? null : field.getName()) + 
			", indexed=" + indexed + 
			", converter=" + converter + " }";
	}
	
	@SuppressWarnings("unchecked")
	public J getValue(Object container) {
		try {
			return getter != null? (J) getter.invoke(container) : (J) field.get(container);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} 
	}
	
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		T annotation = getter == null? null : getter.getAnnotation(annotationClass);
		if (annotation == null && field != null) {
			annotation = field.getAnnotation(annotationClass);
		}
		return annotation;
	}
	
	@Override
	public void setValue(Object container, Object value) {
		try {
			if (setter != null) {
				setter.invoke(container, value);
			} else {
				field.set(container, value);
			}
		} catch (IllegalArgumentException e) {
            throwSetException(value, e);
		} catch (IllegalAccessException e) {
            throwSetException(value, e);
		} catch (InvocationTargetException e) {
            throwSetException(value, e.getTargetException());
		} 
	}

    private void throwSetException(Object value, Throwable cause) {
        String svalue = value == null? null : "(" + value.getClass().getSimpleName() + ") " + value;
        if (setter != null) {
            throw new RuntimeException("Cannot invoke " + setter.getDeclaringClass().getSimpleName() + "." + setter.getName() + "(" + svalue + ")", cause);
        } else {
            throw new RuntimeException("Cannot assign " + field.getDeclaringClass().getSimpleName() + "." + field.getName() + " = " + svalue, cause);
        }
    }
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object convertQueryParam(Object value) {
		Converter converter = getConverter();
		Object convertedValue;
		Class expectedClass;
		if (converter instanceof CollectionConverter) {
			convertedValue = ((CollectionConverter)converter).itemJavaToDatastore(value);
			expectedClass = ((CollectionConverter)converter).getItemType();
		} else {
			convertedValue = converter.javaToDatastore(value);
			expectedClass = getPropertyType();
		}
		if (!ClassUtils.isAssignable(expectedClass, value.getClass())) {
			throw new IllegalArgumentException("Value of " + getName() + " has wrong type. Expected " + expectedClass.getSimpleName() + ", but provided " + value.getClass().getSimpleName());
		}
		return convertedValue;
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

	public Converter<J, D> getConverter() {
		return converter;
	}

	public void setConverter(Converter<J, D> converter) {
		this.converter = converter;
	}

	public Class<J> getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(Class<J> propertyType) {
		this.propertyType = propertyType;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	
}
