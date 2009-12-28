package arden.compiler;

import java.lang.reflect.Method;

import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

/** Contains references to the methods from the ExecutionContext interface */
class ExecutionContextMethods {
	public static final Method write;

	static {
		try {
			write = ExecutionContext.class.getMethod("write", ArdenValue.class);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}