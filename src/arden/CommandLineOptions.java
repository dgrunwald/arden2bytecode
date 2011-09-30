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
			description = "Output file name to compile .MLM file to. \n" +
				"\t  You can also specify a directory in order to compile multiple MLMs.")
	String getOutput();
	boolean isOutput();
	
	@Option(shortName = "a",
			description = "Arguments to MLM if running a MLM.")
	String getArguments();
	boolean isArguments();
	
	@Option(shortName = "e",
			description = "Set arguments to execution environment if \n\t  running a MLM. \n" + 
					"\t  In case of using JDBC, this may be a connection URL e.g. \n" +
					"\t   \"jdbc:mysql://host:port/database?options\".", 
			defaultValue = "stdio")
	String getEnvironment();
	boolean isEnvironment();
	
	@Option(shortName = "d", 
			description = "Class name of database driver to load \n" +
					"\t  (e.g. \"com.mysql.jdbc.Driver\").")
	String getDbdriver();
	boolean isDbdriver();
	
	@Option(shortName = "p",
			description = "Additional classpath. \n"
			+ "\t  E.g. a database driver like \"mysql-connector-java-[version]-bin.jar\".")
	String getClasspath();
	boolean isClasspath();
}
