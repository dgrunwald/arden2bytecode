package arden.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import arden.MainClass;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.compiler.CompiledMlm;

public class BaseExecutionContext extends ExecutionContext {
	List<URL> mlmSearchPath;
	Map<String,ArdenRunnable> moduleList;
	
	public BaseExecutionContext(URL[] mlmSearchPath) {		
		setURLs(mlmSearchPath);
		moduleList = new HashMap<String,ArdenRunnable>();
	}
	
	public void addURL(URL url) {
		mlmSearchPath.add(url);
	}
	
	public void setURLs(URL[] urls) {
		mlmSearchPath = new LinkedList<URL>();
		if (urls != null) {
			mlmSearchPath.addAll(Arrays.asList(urls));
		}
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
		ArdenRunnable fromlist = moduleList.get(name.toLowerCase());
		if (fromlist != null) {
			return fromlist;
		}
		ClassLoader loader = new URLClassLoader(mlmSearchPath.toArray(new URL[]{}));
		InputStream in = loader.getResourceAsStream(name + ".class");
		if (in != null) {
			try {
				ArdenRunnable module = new CompiledMlm(in, name); 
				moduleList.put(name.toLowerCase(), module);
				return module;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		in = loader.getResourceAsStream(name + MainClass.MLM_FILE_EXTENSION);
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
		moduleList.put(name.toLowerCase(), mlm);
		return mlm;
	}
}
