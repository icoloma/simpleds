package org.simpleds.metadata;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

import org.simpleds.converter.Converter;

import com.google.common.collect.Lists;


/**
 * A list of nested {@link SinglePropertyMetadata} instances that point 
 * to a persistent property jumping through intermediate nodes, e.g. foo.bar.baz
 * Used to handle properties provided through embeddedable classes
 * @author icoloma
 */
public class EmbeddedPropertyMetadata implements PropertyMetadata {

	private List<SinglePropertyMetadata> nodes = Lists.newArrayList();
	
	public EmbeddedPropertyMetadata(SinglePropertyMetadata parent, PropertyMetadata child) {
		nodes.add(parent);
		if (child instanceof SinglePropertyMetadata) {
			nodes.add((SinglePropertyMetadata) child);
		} else {
			for (SinglePropertyMetadata node : ((EmbeddedPropertyMetadata)child).getNodes()) {
				nodes.add(node);
			}
		}
			
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName()).append(" { path=");
		for (Iterator<SinglePropertyMetadata> i = nodes.iterator(); i.hasNext(); ) {
			builder.append(i.next().getName());
			if (i.hasNext()) {
				builder.append(".");
			}
		}
		builder.append(", converter=").append(getConverter());
		return builder.append(" }").toString();
	}
	
	public void addProperty(SinglePropertyMetadata property) {
		nodes.add(property);
	}

	SinglePropertyMetadata getLastNode() {
		return nodes.get(nodes.size() - 1);
	}
	
	@Override
	public Converter getConverter() {
		return getLastNode().getConverter();
	}

	@Override
	public String getName() {
		return getLastNode().getName();
	}
	
	@Override
	public boolean isIndexed() {
		return getLastNode().isIndexed();
	}
	
	@Override
	public Object getValue(Object container) {
		for (Iterator<SinglePropertyMetadata> i = nodes.iterator(); i.hasNext(); ) {
			SinglePropertyMetadata node = i.next();
			if (!i.hasNext()) {
				return node.getValue(container);
			}
			container = node.getValue(container);
			if (container == null) {
				return getLastNode().getConverter().getNullValue();
			}
		}
		throw new RuntimeException("Internal error. This code should not be executed");
	}

	@Override
	public void setValue(Object container, Object value) {
		try {
			for (Iterator<SinglePropertyMetadata> i = nodes.iterator(); i.hasNext(); ) {
				SinglePropertyMetadata node = i.next();
				if (!i.hasNext()) {
					node.setValue(container, value);
				} else {
					Object newContainer = node.getValue(container);
					if (newContainer == null) {
						newContainer = node.getPropertyType().newInstance();
						node.setValue(container, newContainer);
					}
					container = newContainer;
				}
			}
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return getLastNode().getAnnotation(annotationClass);
	}
	
	public Class getPropertyType() {
		return getLastNode().getPropertyType();
	}

	public List<SinglePropertyMetadata> getNodes() {
		return nodes;
	}

}
