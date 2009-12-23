package arden.compiler;

/**
 * ClassLoader used for loading the compiled classes without having to save them
 * to disk.
 * 
 * @author Daniel Grunwald
 */
class InMemoryClassLoader extends ClassLoader {
	String className;
	byte[] data;
	Class<?> loadedClass;

	public InMemoryClassLoader(String className, byte[] data) {
		this.className = className;
		this.data = data;
	}

	protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
		if (className.equals(name)) {
			if (loadedClass == null) {
				loadedClass = defineClass(name, data, 0, data.length);
				data = null;
			}
			return loadedClass;
		} else {
			throw new ClassNotFoundException();
		}
	}
}
