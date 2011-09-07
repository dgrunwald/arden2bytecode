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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.compiler.LoadableCompiledMlm;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;

public class MainClass {

	private final static Pattern JAVA_CLASS_NAME = 
		Pattern.compile("[A-Za-z$_][A-Za-z0-9$_]*(?:\\.[A-Za-z$_][A-Za-z0-9$_]*)*");
	
	private final static Pattern MLM_CLASS_FILE_NAME_MATCHER =
		Pattern.compile("(?:[A-Za-z$_][A-Za-z0-9$_]*\\.)*([A-Za-z$_][A-Za-z0-9$_]*)\\.class");
		//Pattern.compile("([A-Za-z$_][A-Za-z0-9$_]*)\\.class");
	
	private final static Pattern CLASS_NAME_FROM_MLM_FILENAME = 
		Pattern.compile("([A-Za-z$_][A-Za-z0-9$_\\.]*)\\.[mM][lL][mM]");
	
	private static List<File> handleInputFileNames(List<String> filenames) {
		List<File> inputFiles = new LinkedList<File>();
		if ((filenames == null) || filenames.isEmpty()) {			
			return null;
		}
		for (String filePath : filenames) {
			File file = new File(filePath);
			if (file.exists()) {
				// file exists => add to input files
				inputFiles.add(file);
			} else {
				// file does not exist => could be a classname rather than a filename
				Matcher m = JAVA_CLASS_NAME.matcher(filePath);
				if (m.matches()) {
					String classFileName = 
						filePath.replace('.', File.separatorChar) 
						+ ".class";
					File classFile  = new File(classFileName);
					if (classFile.exists()) {
						inputFiles.add(classFile);
					} else {
						System.err.println("Class file " + classFileName 
								+ " does not exist.");
					}
				} else {
					System.err.println("File \"" + filePath 
							+ "\" is neither an existing file "
							+ "nor a valid class name.");
				}
			}
		}
		return inputFiles;
	}
	
	private static CompiledMlm compileMlm(File mlmfile, CommandLineOptions options) {
		if (options.getVerbose()) {
			System.out.println("Compiling " + mlmfile.getPath() + " ...");
		}
		
		CompiledMlm mlm = null;
		Compiler compiler = new Compiler();
		try {			
			compiler.enableDebugging(mlmfile.getPath());			
			mlm = compiler.compileMlm(new FileReader(mlmfile.getPath()));
		} catch (CompilerException e) {
			System.err.println("exception compiling " + mlmfile.getPath() + ":");
			e.printStackTrace();
			System.exit(1);
		} catch (FileNotFoundException e) {
			System.err.println("file not found: " + mlmfile.getPath());
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.err.println("IO error reading: " + mlmfile.getPath());
			e.printStackTrace();
			System.exit(1);
		}
		return mlm;
	}
	
	private static ArdenValue[] runMlm(MedicalLogicModule mlm, CommandLineOptions options) {
		ExecutionContext context = new ExecutionContext() {
			@Override
			public void write(ArdenValue message, String destination) {
				System.out.println(message.toString());
			}
		};
		
		ArdenValue[] result = null;
		try {
			result = mlm.run(context, null);
			if (result != null && result.length == 1) {
				System.out.println("Return Value: " + result[0].toString());
			} else {
				System.out.println("There was no return value or result length was not equal to 1.");
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private static int runInputFile(File fileToRun, CommandLineOptions options) {
		String filename = fileToRun.getName();
		MedicalLogicModule mlm = null;
		if (filename.endsWith(".class")) {
			// TODO: load class with ClassLoader (does not work yet)
							
			try {
				mlm = new LoadableCompiledMlm(fileToRun);
			} catch (IOException e) {
				System.err.println("Error loading " +
						fileToRun.getPath());
				e.printStackTrace();
				return 1;
			}
		} else if (fileToRun.getName().endsWith(".mlm")) {
			mlm = compileMlm(fileToRun, options); 
		} else {
			System.err.println("File \"" + fileToRun.getPath() 
					+ "\" is neither .class nor .mlm file.");
			System.err.println("Can't run such a file.");
			return 1;
		}
		
		if (options.getVerbose()) {
			System.out.println("Running MLM...");
			System.out.println("");
		}
		
		ArdenValue[] result = runMlm(mlm, options);
		
		return 0;
	}
	
	private static int compileInputFiles(List<File> inputFiles, CommandLineOptions options) {
		boolean firstFile = true;
		for (File fileToCompile : inputFiles) {
			CompiledMlm mlm = compileMlm(fileToCompile, options);
			File outputFile = null;
			if (options.isOutput() && firstFile) {				
				outputFile = new File(options.getOutput());				
			} else {
				String filename = fileToCompile.getName();
				Matcher m = CLASS_NAME_FROM_MLM_FILENAME.matcher(filename);
				if (m.matches()) {
					String assumedName = m.group(1) + ".class";
					File assumed = new File(fileToCompile.getParentFile(), assumedName);
					if (firstFile) {
						System.err.println("warning: File " + fileToCompile.getName() 
								+ " compiled, but no output file given. Assuming "
								+ assumed.getPath()
								+ " as output file.");
					} else {
						System.err.println("warning: File " + fileToCompile.getName() 
								+ " compiled, but can't write to same output file again. " 
								+ "Assuming "
								+ assumed.getPath()
								+ " as output file.");
					}
					outputFile = assumed;
				} else {
					System.err.println("File " + fileToCompile.getName() 
							+ " compiled, but does not seem to name an MLM file."
							+ " Can't figure out file to write to.");					
				}
			}

			if (outputFile != null) {
				try {
					FileOutputStream fos = new FileOutputStream(outputFile);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					mlm.saveClassFile(bos);
					bos.close();
					fos.close();
				} catch (IOException e) {
					System.err.println("Exception writing output file "
							+ outputFile.getPath() + ":");
					e.printStackTrace();
					return 1;
				}
			}
		}
		return 0;
	}
	
	private static int handleCommandLineArgs(String[] args) {
		// parse command line using jewelCli
		CommandLineOptions options = null;
		try {
			options = CliFactory.parseArguments(CommandLineOptions.class, args);
		} catch (ArgumentValidationException e) {
			String message = e.getMessage();
			System.err.println(message);
			
			if (message.startsWith("Usage")) { // hack to display additional help.
				System.err.println("All further command line arguments that are non-options "
						+ "are regarded as input files.");
				System.err.println("");
			}
			
			return 1;
		}
		
		// suggest using help if no options given
		if (args.length < 1) {
			System.out.println("Supply argument -h or -? to display help.");
			System.out.println("");
		}		
		
		// check input files to this main method
		List<String> files = options.getFiles();
		List<File> inputFiles = handleInputFileNames(files);
		if (inputFiles == null) {
			System.err.println("No input files given.");
			return 1;
		}
		
		if (options.getVerbose()) {
			for (File f : inputFiles) {
				System.out.println("input file: " + f.getPath());
			}
			System.out.println("");
		}
		
		if (options.getRun()) {
			if (inputFiles.size() < 1) {
				System.err.println("You should specify at least one " +
						"MLM or compiled MLM .class file when " +
						"trying to run an MLM.");
				return 1;
			}
			for (File fileToRun : inputFiles) {				
				int result = runInputFile(fileToRun, options);
				if (result != 0) {
					return result;
				}
			}
		} else if (options.getCompile()) {
			return compileInputFiles(inputFiles, options);
		} else {
			// TODO: handle other options
			System.err.println("You should specify -r to run the files or "
					+ "-c to compile the files.");
			System.err.println("Specifying files without telling what to " 
					+ "do with them is not implemented.");
			System.err.println("");
			System.exit(1);
		}
		
		return 0;
	}
	
	public static void main(String[] args) {
		System.out.println("arden2bytecode Compiler");
		System.out.println("Copyright 2010-2011 Daniel Grunwald, Hannes Flicka");
		System.out.println("");
		System.out.println("This program is free software; you can redistribute it and/or modify it");
		System.out.println("under the terms of the GNU General Public License.");
		System.out.println("");
		
		int returnValue = handleCommandLineArgs(args);
		
		System.exit(returnValue);
	}
}
