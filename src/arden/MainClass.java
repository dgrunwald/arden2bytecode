// Arden2ByteCode
// Copyright (c) 2010, Daniel Grunwald, Hannes Flicka
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.compiler.CompiledMlm;
import arden.constants.ConstantParser;
import arden.constants.ConstantParser.ConstantParserException;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.ExpressionHelpers;
import arden.runtime.MedicalLogicModule;
import arden.runtime.StdIOExecutionContext;
import arden.runtime.jdbc.JDBCExecutionContext;

public class MainClass {
	public final static String MLM_FILE_EXTENSION = ".mlm";
	
	private final static String COMPILED_MLM_FILE_EXTENSION = ".class";

	private final static Pattern JAVA_CLASS_NAME = 
		Pattern.compile("[A-Za-z$_][A-Za-z0-9$_]*(?:\\.[A-Za-z$_][A-Za-z0-9$_]*)*");
	
	private CommandLineOptions options;
	
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
						+ COMPILED_MLM_FILE_EXTENSION;
					File classFile  = new File(classFileName);
					if (classFile.exists()) {
						inputFiles.add(classFile);
					} else {
						System.err.println("File " + filePath + " and class file " + classFileName 
								+ " do not exist.");
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
	
	private CompiledMlm compileMlm(File mlmfile) {
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
	
	private ExecutionContext createExecutionContext() {
		if (options.isEnvironment()) {
			if (options.getEnvironment().startsWith("jdbc")) {
				return new JDBCExecutionContext(options);
			} else if ("stdio".equalsIgnoreCase(options.getEnvironment())) {
				return new StdIOExecutionContext(options);
			} else {
				return new StdIOExecutionContext(options);
			}
		} else {
			return new StdIOExecutionContext(options);
		}
	}
	
	private ArdenValue[] getArguments() {
		ArdenValue[] arguments = null;
		if (options.isArguments()) {
			ArdenValue ardenArg = null;
			for (String arg : options.getArguments()) {
				ArdenValue parsedArg = null;
				try {
					parsedArg = ConstantParser.parse(arg);
				} catch (ConstantParserException e) {
					e.printStackTrace();
				}
				if (ardenArg == null) {
					ardenArg = parsedArg;
				} else {
					ardenArg = ExpressionHelpers.binaryComma(ardenArg, parsedArg);
				}
			}
			arguments = new ArdenValue[]{ardenArg};
		}
		return arguments;
	}
	
	private ArdenValue[] runMlm(MedicalLogicModule mlm, ExecutionContext context) {
		ArdenValue[] arguments = getArguments();
		
		ArdenValue[] result = null;
		try {
			result = mlm.run(context, arguments);
			if (result != null && result.length == 1) {
				System.out.println("Return Value: " + result[0].toString());
			} else if (result != null && result.length > 1) {
				for (int i = 0; i < result.length; i++) {
					System.out.println("ReturnValue[" + i + "]: " + result[i].toString());
				}
			} else {
				System.out.println("There was no return value.");
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String getFilenameBase(String filename) {
		int sepindex = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
		int fnindex = filename.lastIndexOf('.');
		if (fnindex < sepindex) {
			fnindex = -1;
		}
		if (fnindex < 0) {
			return filename.substring(sepindex + 1);
		}
		return filename.substring(sepindex + 1, fnindex);	
	}
	
	private MedicalLogicModule getMlmFromFile(File file) {
		String filename = file.getName();
		MedicalLogicModule mlm = null;
		if (filename.endsWith(COMPILED_MLM_FILE_EXTENSION)) {
			// load compiled mlm (.class file)
			try {
				mlm = new CompiledMlm(file, getFilenameBase(filename));
			} catch (IOException e) {
				System.err.println("Error loading " +
						file.getPath());
				e.printStackTrace();
				return null;
			}
		} else if (file.getName().endsWith(MLM_FILE_EXTENSION)) {
			// compile .mlm file
			mlm = compileMlm(file); 
		} else {
			System.err.println("File \"" + file.getPath() 
					+ "\" is neither .class nor .mlm file.");
			System.err.println("Can't run such a file.");
			return null;
		}
		return mlm;
	}
	
	private int runInputFile(File fileToRun) {
		ExecutionContext context = createExecutionContext();

		MedicalLogicModule mlm = getMlmFromFile(fileToRun);
		if (mlm == null) {
			return 1;
		}
		
		if (options.getVerbose()) {
			System.out.println("Running MLM...");
			System.out.println("");
		}
		
		// run the mlm
		runMlm(mlm, context);
		
		return 0;
	}
	
	private int compileInputFiles(List<File> inputFiles) {
		boolean firstFile = true;
		for (File fileToCompile : inputFiles) {
			CompiledMlm mlm = compileMlm(fileToCompile);
			File outputFile = null;
			if (options.isOutput() && firstFile) {
				// output file name given. write to that file...
				outputFile = new File(options.getOutput());				
			} else {
				// output file name unknown. assume mlm name + '.class' extension
				String assumedName = mlm.getName() + COMPILED_MLM_FILE_EXTENSION;
				File assumed = new File(fileToCompile.getParentFile(), assumedName);
				if (firstFile) {
					System.err.println("warning: File " + fileToCompile.getPath() 
							+ " compiled, but no output filename given. Assuming "
							+ assumed.getPath()
							+ " as output file.");
				} else {
					System.err.println("warning: File " + fileToCompile.getPath() 
							+ " compiled, but can't write to same output file again. " 
							+ "Assuming "
							+ assumed.getPath()
							+ " as output file.");
				}
				outputFile = assumed;				
			}

			// if output file is known, compile mlm and write compiled mlm to that file.
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
	
	private static void printLogo() {
		System.out.println("Arden2ByteCode Compiler and Runtime Environment");		
		System.out.println("Copyright 2010-2011 Daniel Grunwald, Hannes Flicka");
		System.out.println("");
		System.out.println("This program is free software; you can redistribute it and/or modify it");
		System.out.println("under the terms of the GNU General Public License.");
		System.out.println("");
	}
	
	private void extendClasspath() {
		String classpath = options.getClasspath();
		String[] paths = classpath.split(File.pathSeparator);
		List<URL> urls = new LinkedList<URL>();
		for (String path : paths) {
			File f = new File(path);
			if (!f.exists()) {
				System.err.println("error: Classpath File/Directory \"" 
						+ path + "\" does not exist.");
				System.exit(1);
			}
			URL url = null;
			try {
				url = f.toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.exit(1);
			}
			if (options.getVerbose()) {
				System.out.println("Adding to classpath: " + url);
			}
			urls.add(url);
		}
		
		ClassLoader currentClassLoader = 
				Thread.currentThread().getContextClassLoader();
		URLClassLoader ulc = new URLClassLoader(
				urls.toArray(new URL[]{}), 
				currentClassLoader);
		Thread.currentThread().setContextClassLoader(ulc);
		
		if (options.getVerbose()) {
			System.out.println();
		}
	}
	
	private int runMlmDaemon(List<File> inputFiles) {
		if (inputFiles.size() < 1) {
			System.err.println("No MLM file specified");
			return 1;
		}
		List<MedicalLogicModule> mlms = new LinkedList<MedicalLogicModule>();
		for (File file : inputFiles) {
			MedicalLogicModule mlm = getMlmFromFile(file);
			if (mlm == null) {
				return 1;
			}
			mlms.add(mlm);
		}
		ExecutionContext context = createExecutionContext();
		ArdenValue[] arguments = getArguments();
		new MlmDaemon(mlms, context, arguments).run();
		return 0;
	}
	
	private int handleCommandLineArgs(String[] args) {
		// parse command line using jewelCli:		
		try {
			options = CliFactory.parseArguments(CommandLineOptions.class, args);
		} catch (ArgumentValidationException e) {
			printLogo();
			String message = e.getMessage();
			System.err.println(message);
			
			if (message.startsWith("Usage")) { // hack to display additional help.
				System.err.println("All further command line arguments that "
						+ "are non-options are regarded as input \n"
						+ "files.");
				System.err.println("For a command-line reference, see:\n"
						+ "http://arden2bytecode.sourceforge.net/docs/"
						+ "arden2bytecode-command-line-reference");
			}
			
			return 1;
		}		
		
		// print logo
		if (!options.getNologo()) {
			printLogo();
		}
		
		if (options.isClasspath()) {
			extendClasspath();
		}
		
		// suggest using help if no options given:
		if (args.length < 1) {
			System.out.println("Supply argument -h or -? to display help.");
			System.out.println("");
		}		
		
		// check input files to this main method:
		List<String> files = options.getFiles();
		List<File> inputFiles = handleInputFileNames(files);
		if (inputFiles == null) {
			System.err.println("No input files given.");
			return 1;
		}
		
		// if verbose output is requested, list input files:
		if (options.getVerbose()) {
			for (File f : inputFiles) {
				System.out.println("Input file: " + f.getPath());
			}
			System.out.println("");
		}
		
		// check if option -r (run) or -c (compile) was given:
		if (options.getRun()) {
			if (inputFiles.size() < 1) {
				System.err.println("You should specify at least one " +
						"MLM or compiled MLM .class file when " +
						"trying to run an MLM.");
				return 1;
			}
			for (File fileToRun : inputFiles) {				
				int result = runInputFile(fileToRun);
				if (result != 0) {
					return result;
				}
			}
		} else if (options.getCompile()) {
			return compileInputFiles(inputFiles);
		} else if (options.getDaemon()) {
			return runMlmDaemon(inputFiles);
		} else {
			System.err.println("You should specify -r to run the files or "
					+ "-c to compile the files.");
			System.err.println("Specifying files without telling what to " 
					+ "do with them is not implemented.");
			return 1;
		}
		
		return 0;
	}
	
	public static void main(String[] args) {		
		int returnValue = 
				new MainClass().handleCommandLineArgs(args);
		
		System.exit(returnValue);
	}
}
