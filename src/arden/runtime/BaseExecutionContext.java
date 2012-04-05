package arden.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.compiler.LoadableCompiledMlm;

public class BaseExecutionContext extends ExecutionContext {
	List<URL> mlmSearchPath;
	
	public BaseExecutionContext(URL[] mlmSearchPath) {
		setURLs(mlmSearchPath);
	}
	
	public void addURL(URL url) {
		mlmSearchPath.add(url);
	}
	
	public void setURLs(URL[] urls) {
		mlmSearchPath = new LinkedList<URL>();
		mlmSearchPath.addAll(Arrays.asList(urls));
	}
	
	@Override
	public void callWithDelay(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delay) {
		System.out.println("delay: " + delay.toString());		
		try {
			mlm.run(this, arguments);
		} catch (InvocationTargetException e) {
			e.printStackTrace();			
		}
	}
	
	@Override
	public ArdenRunnable findModule(String name, String institution) {
		if (!name.matches("[a-zA-Z0-9\\-_]+")) {
			throw new RuntimeException("Malformed module name: " + name);
		}
		ClassLoader loader = new URLClassLoader(mlmSearchPath.toArray(new URL[]{}));
		InputStream in = loader.getResourceAsStream(name + ".class");
		if (in != null) {
			try {
				return new LoadableCompiledMlm(in);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		in = loader.getResourceAsStream(name + ".mlm");
		Compiler compiler = new Compiler();
		MedicalLogicModule mlm = null;
		try {
			mlm = compiler.compile(new InputStreamReader(in, "UTF-8")).get(0);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (CompilerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return mlm;
	}
}
