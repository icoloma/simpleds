package org.simpleds.metadata.asm;


/**
 * A ClassLoader that allows to create your own class
 * @author icoloma
 *
 */
public class ExtensibleClassLoader extends ClassLoader {

	public static ExtensibleClassLoader instance = new ExtensibleClassLoader();
	
	private ExtensibleClassLoader() {}
	
	public Class<?> defineClass(String name, byte[] contents) {
		return defineClass(name, contents, 0, contents.length);
	}

	
}
