package arden;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

@CommandLineInterface(application="arden2bytecode")
public interface CommandLineOptions {
	@Option(shortName = "r", 
			description = "Run MLM file or already compiled MLM class file.")
	boolean getRun();
	
	@Option(shortName = "c", 
			description = "Compile input file.")
	boolean getCompile();
	
	@Option(shortName = "v", 
			description = "Verbose mode.")
	boolean getVerbose();
	
	@Option(shortName = "n", 
			description = "Don't print logo.")
	boolean getNologo();
	
	@Option(helpRequest = true, 
			description = "Display help.", 
			shortName = {"h", "?"})
	boolean getHelp();
	
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
	
	@Option(shortName = "x",
			description = "Set execution environment if running a MLM.", 
			defaultValue = "STDOUT")
	String getEnvironment();
	boolean isEnvironment();
}
