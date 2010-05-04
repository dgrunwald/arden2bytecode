package arden;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;
import arden.runtime.ExecutionContext;

public class MainClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Compiler compiler = new Compiler();
		CompiledMlm mlm;
		try {
			mlm = compiler.compileMlm(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (CompilerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		ExecutionContext context = new ExecutionContext() {
			@Override
			public void write(ArdenValue message, String destination) {
				System.out.println(message.toString());
			}
			
			@Override
			public DatabaseQuery createQuery(String mapping) {
				// TODO Auto-generated method stub
				return super.createQuery(mapping);
			}
			
			// hier weitere Methoden von ExecutionContext überschreiben
		};
		ArdenValue[] arguments = { new ArdenNumber(3), new ArdenNumber(99), new ArdenNumber(140), new ArdenString("a") };
		try {
			mlm.run(context, arguments);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
