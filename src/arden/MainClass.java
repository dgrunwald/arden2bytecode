// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.compiler.RawCompiledMlm;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;

public class MainClass {

	private final static Pattern JAVA_CLASS_NAME = 
		Pattern.compile("[A-Za-z$_][A-Za-z0-9$_]*(?:\\.[A-Za-z$_][A-Za-z0-9$_]*)*");
	
	private final static Pattern MLM_NAME_MATCHER =
		Pattern.compile("(?:[A-Za-z$_][A-Za-z0-9$_]*\\.)*([A-Za-z$_][A-Za-z0-9$_]*)\\.class");
	
	public static void main(String[] args) {
		System.out.println("arden2bytecode Compiler");
		System.out.println("Copyright 2011 Daniel Grunwald, Hannes Flicka");
		System.out.println("");
		System.out.println("This program is free software; you can redistribute it and/or modify it");
		System.out.println("under the terms of the GNU General Public License.");
		System.out.println("");
		System.out.println("Supply argument -h or -? to display help.");
		System.out.println("");

		List<File> inputFiles = new LinkedList<File>();

		CommandLineOptions options = null;
		try {
			options = CliFactory.parseArguments(CommandLineOptions.class, args);
		} catch (ArgumentValidationException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		// check input files to this compiler
		for (String filePath : options.getFiles()) {
			File file = new File(filePath);
			if (file.exists()) {
				inputFiles.add(file);
			} else {
				Matcher m = JAVA_CLASS_NAME.matcher(filePath);
				if (m.matches()) {
					inputFiles.add(new File("." + 
							File.separatorChar + 
							filePath.replace('.', File.separatorChar) +
							".class"));
				}
			}
		}
		
		if (options.getRun()) {
			if (inputFiles.size() != 1) {
				System.err.println("You should specify exactly one " +
						"MLM or compiled MLM .class file when " +
						"trying to run an MLM.");
				System.exit(1);
			}
			File fileToRun = inputFiles.get(0);
			MedicalLogicModule mlmToRun = null;
			if (fileToRun.getName().endsWith(".class")) {
				// TODO: load class with ClassLoader
				Matcher m = MLM_NAME_MATCHER.matcher(fileToRun.getName());
				if (!m.matches()) {
					String mlmname = m.group();					
					try {
						mlmToRun = new RawCompiledMlm(fileToRun, mlmname);
					} catch (IOException e) {
						System.err.println("Error loading " +
								fileToRun.getName());
						e.printStackTrace();
					}
				}
			} else if (fileToRun.getName().endsWith(".mlm")) {
				Compiler compiler = new Compiler();
				try {
					compiler.enableDebugging(fileToRun.getPath());
					mlmToRun = compiler.compileMlm(new FileReader(fileToRun.getPath()));
				} catch (CompilerException e) {
					System.err.println("exception compiling " + fileToRun.getPath() + ":");
					e.printStackTrace();
					System.exit(1);
				} catch (FileNotFoundException e) {
					System.err.println("file not found: " + fileToRun.getPath());
					e.printStackTrace();
					System.exit(1);
				} catch (IOException e) {
					System.err.println("IO error reading: " + fileToRun.getPath());
					e.printStackTrace();
					System.exit(1);
				} 
			}

			ExecutionContext context = new ExecutionContext() {
				@Override
				public void write(ArdenValue message, String destination) {
					System.out.println(message.toString());
				}
			};

			try {
				ArdenValue[] result = mlmToRun.run(context, null);
				if (result != null && result.length == 1) {
					System.out.println("Return Value: " + result[0].toString());
				} else {
					System.out.println("There was no return value or result length was not equal to 1.");
				}
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			// TODO: handle other options
		}
		
		
		/*
		Compiler compiler = new Compiler();
		CompiledMlm mlm;
		try {
			compiler.enableDebugging(args[0]);
			mlm = compiler.compileMlm(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			System.err.println("error: File " + args[0] + " was not found.");
			return;
		} catch (CompilerException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("error: No MLM source file given.");
			System.err.println("usage: java <arden2bytecode program> <MLM-file>");
			System.err.println("");
			System.err.println("e.g.: java -jar arden2bytecode.jar <MLM-file>");
			System.err.println("or: java arden.MainClass <MLM-file>");
			return;
		}
		ExecutionContext context = new ExecutionContext() {
			@Override
			public void write(ArdenValue message, String destination) {
				System.out.println(message.toString());
			}
		};
		try {
			ArdenValue[] result = mlm.run(context, null);
			if (result != null && result.length == 1) {
				System.out.println("Return Value: " + result[0].toString());
			} else {
				System.out.println("There was no return value or result length was not equal to 1.");
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		*/
	}
}
