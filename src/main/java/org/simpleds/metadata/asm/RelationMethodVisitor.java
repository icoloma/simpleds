package org.simpleds.metadata.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.util.CheckAnnotationAdapter;
import org.simpleds.annotations.One;

/**
 * If the method is annotated as {@link One}, add the intercepting code to calculate the relation
 * @author icoloma
 */
public class RelationMethodVisitor extends EmptyMethodAdapter {

	private int access;
	
	private String name;
	
	private String descriptor;
	
	private String signature;
	
	private String[] exceptions;
	
	/** true only if this  method  should be generated */
	private boolean apply;
	
	/** the containing class */
	private EnhancerVisitor classVisitor;
	
	/** the name of the attribute to use */
	private String attributeName;

	public RelationMethodVisitor(EnhancerVisitor classVisitor, int access, String name, String descriptor,
			String signature, String[] exceptions) {
		this.access = access;
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
		this.exceptions = exceptions;
		this.classVisitor = classVisitor;
		this.attributeName = (name.startsWith("get") && name.length() > 3? Character.toLowerCase(name.charAt(3)) + name.substring(4) : name) + "Key";
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		AnnotationVisitor va = super.visitAnnotation(desc, visible);
		if (!desc.equals("Lorg/simpleds/annotations/One;")) {
			return null;
		}
		
		this.apply = true;
		return new CheckAnnotationAdapter(va) {
			@Override
			public void visit(String name, Object value) {
				if (name == "value") {
					attributeName = (String) value;
				}
				super.visit(name, value);
			}
		};
	}
	
	@Override
	public void visitEnd() {
		if (apply) {
			classVisitor.createDelegateMethod(this);
		}
	}

	public int getAccess() {
		return access;
	}

	public String getName() {
		return name;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public String getSignature() {
		return signature;
	}

	public String[] getExceptions() {
		return exceptions;
	}

	public boolean isApply() {
		return apply;
	}

	public EnhancerVisitor getClassVisitor() {
		return classVisitor;
	}

	public String getAttributeName() {
		return attributeName;
	}
	
}
