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
import arden.runtime.ExecutionContext;

public class MainClass {

	public static void main(String[] args) {
		System.out.println("arden2bytecode Interpreter example");
		System.out.println("Copyright 2010 Daniel Grunwald");
		System.out.println("");
		System.out.println("This program is free software; you can redistribute it and/or modify it");
		System.out.println("under the terms of the GNU General Public License.");
		System.out.println("");

		Compiler compiler = new Compiler();
		CompiledMlm mlm;
		try {
			compiler.enableDebugging(args[0]);
			mlm = compiler.compileMlm(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (CompilerException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		ExecutionContext context = new ExecutionContext() {
			@Override
			public void write(ArdenValue message, String destination) {
				System.out.println(message.toString());
			}
		};
		try {
			ArdenValue result = mlm.run(context, null);
			if (result != null)
				System.out.println("Return Value: " + result.toString());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
