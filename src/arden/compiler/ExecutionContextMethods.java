package arden.compiler;

import java.lang.reflect.Method;

import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

/** Contains references to the methods from the ExecutionContext class */
final class ExecutionContextMethods {
	public static final Method write;
	public static final Method getEventTime, getTriggerTime, getCurrentTime;

	static {
		try {
			write = ExecutionContext.class.getMethod("write", ArdenValue.class);
			getEventTime = ExecutionContext.class.getMethod("getEventTime");
			getTriggerTime = ExecutionContext.class.getMethod("getTriggerTime");
			getCurrentTime = ExecutionContext.class.getMethod("getCurrentTime");
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
