package arden.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;

import arden.CommandLineOptions;
import arden.constants.ConstantParser;
import arden.constants.ConstantParser.ConstantParserException;

public class StdIOExecutionContext extends BaseExecutionContext {
	@SuppressWarnings("unused")
	private CommandLineOptions options;
	
	public StdIOExecutionContext(CommandLineOptions options) {
		super(new URL[]{});
		this.options = options;	
		try {
			addURL(new File(".").toURI().toURL());
			if (options.isClasspath()) {
				String[] paths = options.getClasspath().split(File.pathSeparator);
				for (String path : paths) {
					File f = new File(path);
					URL url = null;
					url = f.toURI().toURL();
					addURL(url);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();	
		}
	}
	
	public DatabaseQuery createQuery(String mapping) {
		System.out.println("Query mapping: \"" + mapping + "\". Enter result as " +
				"constant Arden Syntax expression (Strings in quotes)");
		System.out.print(" >");
		Scanner sc = new Scanner(System.in);
		String line = null;
		if (sc.hasNext()) {
			line = sc.nextLine();
		}
		ArdenValue[] val = null;
		try {
			val = new ArdenValue[] {
				ConstantParser.parse(line)
			};
		} catch (ConstantParserException e) {
			System.out.println("Error parsing at char: " + e.getPos());
			System.out.println("Message: " + e.getMessage());
		}
		return new MemoryQuery(val);
	}
	
	public ArdenValue getMessage(String mapping) {
		System.out.println("Message, mapping: " + mapping);
		return new ArdenString(mapping);
	}
	
	public void write(ArdenValue message, String destination) {
		if ("stdout".equalsIgnoreCase(destination)) {
			// just print string
			if (message instanceof ArdenString) {
				System.out.println(ArdenString.getStringFromValue(message));
			} else {
				System.out.println(message);
			}
		} else {
			// prepend destination to printed string
			System.out.print("Destination: \"");
			System.out.print(destination);
			System.out.print("\" Message: ");
			if (message instanceof ArdenString) {
				System.out.println(ArdenString.getStringFromValue(message));
			} else {
				System.out.println(message);
			}
		}
	}
	
	private ArdenTime eventtime = new ArdenTime(new Date());
	
	public ArdenTime getEventTime() {
		return eventtime;
	}

	public ArdenTime getTriggerTime() {
		return eventtime;
	}

	public ArdenTime getCurrentTime() {
		return new ArdenTime(new Date());
	}
}
