package org.simpleds.metadata.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.simpleds.annotations.One;

/**
 * Enhances a persistent class adding interceptors to all {@link One} methods
 * @author icoloma
 *
 */
public class EnhancerVisitor extends EmptyClassAdapter implements Opcodes {

	/** return true if the written class was enhanced */
	private boolean enhanced = false;
	
	private String className;
	
	private String parentName;
	
	private ClassVisitor cv;
	
	public EnhancerVisitor(ClassVisitor cv) {
		this.cv = cv;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		cv.visit(version, access, name + "__ds__", signature, name, interfaces);
		this.className = name + "__ds__";
		this.parentName = name;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor,
			String signature, String[] exceptions) {
		if ((access & Opcodes.ACC_STATIC) == 1 || Type.getArgumentTypes(descriptor).length != 0) {
			return null;
		}
		if ("<init>".equals(name)) {
			createConstructor(exceptions);
		}
		return new RelationMethodVisitor(this, access, name, descriptor, signature, exceptions);
	}
	
	/**
	 * Create a default constructor
	 */
	public void createConstructor(String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, exceptions);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, parentName, "<init>", "()V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	/**
	 * Create a relationship method in the child class
	 */
	public void createDelegateMethod(RelationMethodVisitor visitor) {
		enhanced = true;
		
		Type returnType = Type.getReturnType(visitor.getDescriptor());
		MethodVisitor mv = cv.visitMethod(visitor.getAccess(), visitor.getName(), visitor.getDescriptor(), visitor.getSignature(), visitor.getExceptions());
		mv.visitCode();
		mv.visitMethodInsn(INVOKESTATIC, "org/simpleds/EntityManagerFactory", "getEntityManager", "()Lorg/simpleds/EntityManager;");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, visitor.getAttributeName(), "Lcom/google/appengine/api/datastore/Key;");
		mv.visitMethodInsn(INVOKEINTERFACE, "org/simpleds/EntityManager", "get", "(Lcom/google/appengine/api/datastore/Key;)Ljava/lang/Object;");
		mv.visitTypeInsn(CHECKCAST, returnType.getInternalName());
		mv.visitInsn(ARETURN);
		mv.visitMaxs(2, 1);
		mv.visitEnd();

		// TODO set all involved attributes to visibility (at least) protected
	}
	
	@Override
	public void visitEnd() {
		cv.visitEnd();
	}
	
	public boolean wasEnhanced() {
		return enhanced;
	}

}
