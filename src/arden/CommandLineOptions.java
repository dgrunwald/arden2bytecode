package arden;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

@CommandLineInterface(application="arden2bytecode")
public interface CommandLineOptions {
	@Option(shortName = "r", 
			description = "run MLM file or already compiled MLM class file")
	boolean getRun();
	
	@Option(shortName = "c", 
			description = "compile input file")
	boolean getCompile();
	
	@Option(shortName = "v", 
			description = "verbose mode")
	boolean getVerbose();	
	
	@Option(helpRequest = true, 
			description = "display help", 
			shortName = {"h", "?"})
	boolean getHelp();
	
	/*@Option(shortName = "f", 
			description = "input files (MLM files or MLM class files)")
	List<String> getInputFiles();
	boolean isInputFiles();*/
	
	@Unparsed
	List<String> getFiles();
	boolean isFiles();
	
	@Option(shortName = "o",
			description = "Output file name to compile .MLM file to. You can also specify a directory e.g. in order to compile multiple MLMs.")
	String getOutput();
	boolean isOutput();
	
	@Option(shortName = "a",
			description = "Arguments to MLM if running a MLM.")
	String getArguments();
	boolean isArguments();
	
	/*
	@Option(shortName = "t",
			defaultValue = "15",
			description = "worker thread count (default = 15)")
	int getThreadCount();
	
	@Option(shortName = "l",
			description = "log file name")
	String getLogFile();
	boolean isLogFile();*/	
}
